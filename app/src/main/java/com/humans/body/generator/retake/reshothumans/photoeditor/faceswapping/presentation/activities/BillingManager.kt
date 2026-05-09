package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams


class BillingManager (private val context: Context, var listener: PurchaseListener) {

    interface PurchaseListener {
        fun onPurchaseSuccess()
        fun onPurchaseFailure()
    }

    private lateinit var billingClient: BillingClient

    fun startConnection(onConnected: () -> Unit) {
        if (::billingClient.isInitialized && billingClient.isReady) {
            onConnected()
            return
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else {
                    Log.e("BillingManager", "Purchase failed or is null: ${billingResult.debugMessage}")
                }
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Billing setup finished successfully.")
                    onConnected()
                } else {
                    Log.e("BillingManager", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingManager", "Billing service disconnected. Reconnecting...")
                startConnection(onConnected)
            }
        })
    }

    fun querySubscriptions(productIds: List<String>, callback: (List<ProductDetails>) -> Unit) {
        if (!::billingClient.isInitialized || !billingClient.isReady) {
            Log.w("BillingManager", "BillingClient not ready in querySubscriptions()")
            callback(emptyList())
            return
        }

        val products = productIds.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                callback(productDetailsResult.productDetailsList)
            } else {
                Log.e("BillingManager", "queryProductDetailsAsync failed: ${billingResult.debugMessage}")
                callback(emptyList())
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            Log.d("BillingManager", "No offer token available for product ${productDetails.productId}")
            return
        }

        Log.d("BillingManager", "Launching purchase flow for ${productDetails.productId} with offerToken $offerToken")

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun checkSubscriptionStatus(callback: (Boolean) -> Unit) {
        if (!::billingClient.isInitialized || !billingClient.isReady) {
            Log.w("BillingManager", "BillingClient not ready in checkSubscriptionStatus()")
            callback(false)
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isSubscribed = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                Log.d("BillingManager", "Subscription status: $isSubscribed")
                callback(isSubscribed)
            } else {
                Log.e("BillingManager", "checkSubscriptionStatus failed: ${billingResult.debugMessage}")
                callback(false)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("BillingManager", "Purchase acknowledged successfully")
                        listener.onPurchaseSuccess()
                    } else {
                        Log.e("BillingManager", "Acknowledge failed: ${billingResult.debugMessage}")
                        listener.onPurchaseFailure()
                    }
                }
            } else {
                Log.d("BillingManager", "Purchase already acknowledged")
                listener.onPurchaseSuccess()
            }
        }
    }
}
