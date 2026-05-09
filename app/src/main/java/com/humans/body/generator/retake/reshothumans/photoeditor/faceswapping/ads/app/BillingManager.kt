package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.Constants.PRODUCT_LIFETIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    private val context: Application,
    private val adsPref: AdsPref,
    private val premiumRepository: PremiumRepository
) {
    private lateinit var billingClient: BillingClient
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize() {
        // Set initial state from local storage
        val localPremiumStatus = adsPref.getIsPremiumStatus()
        premiumRepository.updatePremiumState(localPremiumStatus)

        // Setup billing client and sync with server
        setupBillingClient()
    }

    private fun setupBillingClient() {
        Timber.d("Billing: Initializing BillingClient")

        billingClient = BillingClient.newBuilder(context)
            .setListener { result, purchases -> handlePurchaseUpdate(result, purchases) }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        connectBillingClient()
    }

    private fun connectBillingClient() {
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    Timber.d("Billing: Setup finished - ${result.responseCode}")

                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryAllPurchases()
                        retryUnacknowledgedPurchases()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.w("Billing: Service disconnected, retrying in 1s")
                    mainHandler.postDelayed({ connectBillingClient() }, 1000)
                }
            })
        } catch (e: SecurityException) {
            Timber.e(e, "Billing: SecurityException during connection (likely cross-user/work profile restriction)")
        } catch (e: Exception) {
            Timber.e(e, "Billing: Unexpected error during startConnection")
        }
    }

    fun queryAllPurchases() {
        val productTypes = listOf(
            BillingClient.ProductType.SUBS,
            BillingClient.ProductType.INAPP
        )

        val allPurchases = mutableListOf<Purchase>()
        val countdown = CountDownLatch(productTypes.size)

        productTypes.forEach { type ->
            queryPurchasesByType(type) { purchases ->
                synchronized(allPurchases) {
                    allPurchases.addAll(purchases)
                }
                countdown.countDown()
            }
        }

        // Process all purchases after both queries complete
        CoroutineScope(Dispatchers.IO).launch {
            countdown.await()
            processPurchases(allPurchases)
        }
    }

    private fun queryPurchasesByType(
        type: String,
        onComplete: (List<Purchase>) -> Unit
    ) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(type).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onComplete(purchases)
            } else {
                Timber.w("Query failed for $type: ${result.debugMessage}")
                onComplete(emptyList())
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val validPurchases = purchases.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        val purchasedProducts = mutableSetOf<String>()
        var hasLifetime = false

        validPurchases.forEach { purchase ->
            val productId = purchase.products.firstOrNull() ?: return@forEach

            // Acknowledge if needed
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }

            when (productId) {
                PRODUCT_LIFETIME -> {
                    hasLifetime = true
                    purchasedProducts.clear()
                    purchasedProducts.add(PRODUCT_LIFETIME)
                }
                in PREMIUM_PRODUCTS -> {
                    if (!hasLifetime) {
                        purchasedProducts.add(productId)
                    }
                }
            }
        }

        val isPremium = purchasedProducts.isNotEmpty()
        adsPref.setPurchasedProductsSet(purchasedProducts)
        adsPref.setIsPremiumStatus(isPremium)
        premiumRepository.updatePremiumState(isPremium)

        Timber.d("Purchases processed: $purchasedProducts, isPremium=$isPremium")
    }

    private fun handlePurchaseUpdate(result: BillingResult, purchases: List<Purchase>?) {
        Timber.d("Purchase update: ${result.responseCode}")

        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, attemptsLeft: Int = 3) {
        if (attemptsLeft <= 0) {
            Timber.e("Failed to acknowledge: ${purchase.products}")
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("Purchase acknowledged: ${purchase.products}")
            } else {
                Timber.w("Acknowledge failed, retrying: ${result.debugMessage}")
                mainHandler.postDelayed({
                    acknowledgePurchase(purchase, attemptsLeft - 1)
                }, 2000)
            }
        }
    }

    private fun retryUnacknowledgedPurchases() {
        queryAllPurchases() // Reuses existing acknowledgment logic
    }

    companion object {
        private val PREMIUM_PRODUCTS = setOf(
            Constants.PRODUCT_WEEKLY,
            PRODUCT_LIFETIME,
            Constants.PRODUCT_MONTHLY,
            Constants.PRODUCT_YEARLY
        )
    }
}