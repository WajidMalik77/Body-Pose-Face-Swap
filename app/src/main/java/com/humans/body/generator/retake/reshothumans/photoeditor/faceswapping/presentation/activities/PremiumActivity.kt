package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivityPremiumBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.RemoteScreens
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdsManagerEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.PrefsName
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.isFirstTime
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.monthly_ad
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.weekly_ad
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.yearly_ad
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.GooglePlayBuySubscription
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SubscriptionBilling
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SubscriptionPurchaseInterface
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.openPrivacyPolicy
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

class PremiumActivity : BaseActivity(), SubscriptionPurchaseInterface {
    private val binding by lazy {
        ActivityPremiumBinding.inflate(layoutInflater)
    }
    private var selectedProductId: String = yearly_ad
    var check: String = ""

    private var isSplash = false
    private var isFirstPremiumFlow = false

    private var billingClient: BillingClient? = null
    private val productDetailsById: MutableMap<String, ProductDetails> = linkedMapOf()
    var retryCount = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.premium_bg_color)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        isSplash = intent.getBooleanExtra("isSplash", false)
        isFirstPremiumFlow = intent.getBooleanExtra("isFirstPremiumFlow", false)
        Handler(mainLooper).postDelayed({
            binding.back.visibility = View.VISIBLE
        }, 1400)

        binding.terms.setOnClickListener {
            openPrivacyPolicy("https://hnitechai.wordpress.com/human-art-terms-condition/")
        }
        binding.privacyPolicyPremium.setOnClickListener {
            openPrivacyPolicy("https://hnitechai.wordpress.com/hni-technologies-privacy-policy/")
        }
        binding.back.setOnClickListener {
            goNext()
        }

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        verifySubPurchase(purchase)
                    }
                }
            }.build()
        establishConnection()

        GooglePlayBuySubscription.purchasesInterface = this
        Log.e("TAG", "onCreate0: ${SubscriptionBilling.productsDetailsList.isNullOrEmpty()}")
        if (!SubscriptionBilling.productsDetailsList.isNullOrEmpty()) {

            SubscriptionBilling.productsDetailsList?.let { list ->

                val map = list.associateBy { it.productId }

                map[yearly_ad]?.let {
                    binding.sixMonthPrice.text = getPrice(it)
                }

                map[monthly_ad]?.let {
                    binding.yearlyPrice.text = getPrice(it)
                }

                map[weekly_ad]?.let {
                    binding.lifetimePrice.text = getPrice(it)
                }
            }
        } else {
            Log.e("TAG", "onCreate0: ELSE")
        }

        binding.continueBtn.setOnClickListener {
            val selected = productDetailsById[selectedProductId]
            Log.d(
                "PremiumFlow",
                "Continue clicked. selectedProductId=$selectedProductId availableProductIds=${productDetailsById.keys}"
            )
            selected?.let { product ->
                Log.d("PremiumFlow", "Launching billing for productId=${product.productId}")
                launchPurchaseFlow(product)
            } ?: Log.w("PremiumFlow", "No ProductDetails found for selectedProductId=$selectedProductId")
        }

        binding.sixMonth.setOnClickListener {
            selectedProductId = yearly_ad
            Log.d("PremiumFlow", "Plan selected: sixMonth -> productId=$selectedProductId")
            binding.yearly.setBackgroundResource(R.drawable.premium_unselected)
            binding.sixMonth.setBackgroundResource(R.drawable.premium_selected)
            binding.lifetime.setBackgroundResource(R.drawable.premium_unselected)
        }

        binding.yearly.setOnClickListener {
            selectedProductId = monthly_ad
            Log.d("PremiumFlow", "Plan selected: yearly -> productId=$selectedProductId")
            binding.yearly.setBackgroundResource(R.drawable.premium_selected)
            binding.sixMonth.setBackgroundResource(R.drawable.premium_unselected)
            binding.lifetime.setBackgroundResource(R.drawable.premium_unselected)
        }

        binding.lifetime.setOnClickListener {
            selectedProductId = weekly_ad
            Log.d("PremiumFlow", "Plan selected: lifetime -> productId=$selectedProductId")
            binding.yearly.setBackgroundResource(R.drawable.premium_unselected)
            binding.sixMonth.setBackgroundResource(R.drawable.premium_unselected)
            binding.lifetime.setBackgroundResource(R.drawable.premium_selected)
        }
    }

    private fun restorePurchases() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://play.google.com/store/account/subscriptions".toUri()
            setPackage("com.android.vending")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/account/subscriptions".toUri()
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun goNext() {
        if (isSplash) {
            if (!isFinishing && !isDestroyed) {
                val nextIntent = Intent(this, HomeActivity::class.java)
                showBypassInterstitialAndNavigate(RemoteScreens.PREMIUM_SCREEN, "close", nextIntent)
            }
        } else {
            finish()
        }
    }

    override fun productPurchasedSuccessful() {
        PrefUtil.setPremium(this, true)
        Toast.makeText(this, "subscribed_successfully", Toast.LENGTH_SHORT).show()
        goNext()
    }

    override fun productPurchaseFailed() {
        Toast.makeText(this@PremiumActivity, "subscription_failed", Toast.LENGTH_LONG).show()
    }

    private fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    getProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retryCount <= 3) establishConnection()
                retryCount++
            }
        })
    }

    private fun verifySubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // ✅ FIXED: was PrefUtil(this).setBool("is_premium", true) — wrong key
                PrefUtil.setPremium(this, true)
                Toast.makeText(this, "verifySubPurchase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(yearly_ad)
                .setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(monthly_ad)
                .setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(weekly_ad)
                .setProductType(BillingClient.ProductType.SUBS).build(),
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient!!.queryProductDetailsAsync(params) { _, productDetailsResult ->
            productDetailsById.clear()
            productDetailsResult.productDetailsList.forEach {
                Log.d("Hassan", "getProducts: ${it.productId}")
                productDetailsById[it.productId] = it
            }
        }
    }

    private fun getPrice(prod: ProductDetails): String {
        val offer = prod.subscriptionOfferDetails?.firstOrNull()
        val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        return "${phase?.formattedPrice} ${phase?.priceCurrencyCode}"
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        MyApplication.isResume = true
        assert(productDetails.subscriptionOfferDetails != null)
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken).build()
        )
        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                .build()
        billingClient!!.launchBillingFlow(this@PremiumActivity, billingFlowParams)
    }

    override fun onResume() {
        super.onResume()
        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifySubPurchase(purchase)
                    }
                }
            }
        }
    }

    private fun showBypassInterstitialAndNavigate(screen: String, trigger: String, nextIntent: Intent) {
        if (!isFirstPremiumFlow) {
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(nextIntent)
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)
        lifecycleScope.launch {
            entryPoint.adsManager().showInterstitialAd(
                activity = this@PremiumActivity,
                screen = screen,
                trigger = trigger,
                noCounterNeeded = true,
                onAdClosed = {
                    if (!isFinishing) {
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(nextIntent)
                        overridePendingTransition(0, 0)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                },
                onAdNotShown = {
                    if (!isFinishing) {
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(nextIntent)
                        overridePendingTransition(0, 0)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                }
            )
        }
    }
}
