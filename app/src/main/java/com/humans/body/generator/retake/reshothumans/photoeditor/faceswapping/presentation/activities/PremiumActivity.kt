package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.annotation.SuppressLint
import android.content.Context
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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.RemoteScreens
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdsManagerEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivityPremiumBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.monthly_ad
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.weekly_ad
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.yearly_ad
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.openPrivacyPolicy
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.FunnelAnalytics
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.PurchaseAnalyticsLogger
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import timber.log.Timber

class PremiumActivity : BaseActivity() {
    companion object {
        private const val BILLING_DIAG_TAG = "BillingDiag"
    }

    private val binding by lazy {
        ActivityPremiumBinding.inflate(layoutInflater)
    }
    private var selectedProductId: String = yearly_ad
    var check: String = ""

    private var isSplash = false
    private var isFirstPremiumFlow = false

    private var billingClient: BillingClient? = null
    private val productDetailsById: MutableMap<String, ProductDetails> = linkedMapOf()
    private val purchasedProductIds: MutableSet<String> = mutableSetOf()

    /** Token of the user's current active subscription (if any). Required for upgrades. */
    private var currentSubPurchaseToken: String? = null
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
            FunnelAnalytics.logScreenEvent(this, "premium", "click_terms")
            openPrivacyPolicy("https://hnitechai.wordpress.com/human-art-terms-condition/")
        }
        binding.privacyPolicyPremium.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_privacy")
            openPrivacyPolicy("https://hnitechai.wordpress.com/hni-technologies-privacy-policy/")
        }
        binding.back.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_back")
            goNext()
        }
        binding.manageSubscription.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_manage_subscription")
            restorePurchases()
        }
        setupDevPremiumToggle()
        logBillingDiagnostics("premium_onCreate_before_billing_setup")
        PurchaseAnalyticsLogger.logPaywallViewed("premium_activity", if (isSplash) "splash" else "in_app")

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
            .setListener { billingResult, list ->
                Log.d(
                    BILLING_DIAG_TAG,
                    "purchasesUpdated responseCode=${billingResult.responseCode} debugMessage=${billingResult.debugMessage} purchasesCount=${list?.size ?: 0}"
                )
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> list?.forEach { purchase ->
                        PurchaseAnalyticsLogger.logPurchaseResult(
                            productId = purchase.products.firstOrNull(),
                            productType = BillingClient.ProductType.SUBS,
                            result = "success",
                            billingResult = billingResult,
                            token = purchase.purchaseToken,
                            isAcknowledged = purchase.isAcknowledged,
                            source = "premium_activity"
                        )
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseAnalyticsLogger.logPurchaseResult(
                        productId = selectedProductId,
                        productType = BillingClient.ProductType.SUBS,
                        result = "cancel",
                        billingResult = billingResult,
                        source = "premium_activity"
                    )
                    else -> PurchaseAnalyticsLogger.logPurchaseResult(
                        productId = selectedProductId,
                        productType = BillingClient.ProductType.SUBS,
                        result = "error",
                        billingResult = billingResult,
                        source = "premium_activity"
                    )
                }
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        verifySubPurchase(purchase)
                    }
                }
            }.build()
        establishConnection()

        // Prices are populated from productDetailsById once getProducts() completes — see refreshPrices().

        binding.continueBtn.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_continue")
            PurchaseAnalyticsLogger.logPurchaseTap(
                productId = selectedProductId,
                productType = BillingClient.ProductType.SUBS,
                screen = "premium_activity",
                source = if (isSplash) "splash" else "in_app"
            )
            if (purchasedProductIds.contains(selectedProductId)) {
                runOnUiThread { updatePurchasedCardsUi() }
                Toast.makeText(this, "This plan is already purchased", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selected = productDetailsById[selectedProductId]
            Timber.d(
                "Continue clicked. selectedProductId=$selectedProductId availableProductIds=${productDetailsById.keys}"
            )
            selected?.let { product ->
                Timber.d("Launching billing for productId=${product.productId}")
                launchPurchaseFlow(product)
            } ?: Timber.d(
                "No ProductDetails found for selectedProductId=$selectedProductId"
            )
        }

        binding.sixMonth.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_plan_yearly")
            if (purchasedProductIds.contains(yearly_ad)) return@setOnClickListener
            selectedProductId = yearly_ad
            Timber.d("Plan selected: sixMonth -> productId=$selectedProductId")
            binding.yearly.setBackgroundResource(R.drawable.premium_unselected)
            binding.sixMonth.setBackgroundResource(R.drawable.premium_selected)
            binding.lifetime.setBackgroundResource(R.drawable.premium_unselected)
        }

        binding.yearly.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_plan_monthly")
            if (purchasedProductIds.contains(monthly_ad)) return@setOnClickListener
            selectedProductId = monthly_ad
            Timber.d("Plan selected: yearly -> productId=$selectedProductId")
            binding.yearly.setBackgroundResource(R.drawable.premium_selected)
            binding.sixMonth.setBackgroundResource(R.drawable.premium_unselected)
            binding.lifetime.setBackgroundResource(R.drawable.premium_unselected)
        }

        binding.lifetime.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "premium", "click_plan_weekly")
            if (purchasedProductIds.contains(weekly_ad)) return@setOnClickListener
            selectedProductId = weekly_ad
            Timber.d("Plan selected: lifetime -> productId=$selectedProductId")
            binding.yearly.setBackgroundResource(R.drawable.premium_unselected)
            binding.sixMonth.setBackgroundResource(R.drawable.premium_unselected)
            binding.lifetime.setBackgroundResource(R.drawable.premium_selected)
        }

        FunnelAnalytics.logScreenEvent(this, "premium", "on_create")
    }

    override fun onDestroy() {
        FunnelAnalytics.logScreenEvent(this, "premium", "on_destroy")
        super.onDestroy()
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

    private fun setupDevPremiumToggle() {
        if (!BuildConfig.SHOW_PREMIUM_DEV_BUTTON) {
            binding.devPremiumToggleBtn.visibility = View.GONE
            return
        }
        binding.devPremiumToggleBtn.visibility = View.VISIBLE
        updateDevPremiumButtonText()
        binding.devPremiumToggleBtn.setOnClickListener {
            val newState = !PrefUtil.isPremium(this)
            PrefUtil.setPremium(this, newState)
            updateDevPremiumButtonText()
            Toast.makeText(this, "Simulated premium: $newState", Toast.LENGTH_SHORT).show()
            if (newState) {
                goNext()
            }
        }
    }

    private fun updateDevPremiumButtonText() {
        val isPremium = PrefUtil.isPremium(this)
        binding.devPremiumToggleBtn.text =
            if (isPremium) "Simulate Premium: ON" else "Simulate Premium: OFF"
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

    private fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Timber.d(
                    "onBillingSetupFinished responseCode=${billingResult.responseCode} debugMessage=${billingResult.debugMessage}"
                )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    syncPurchasedSubscriptions()
                    getProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retryCount <= 3) establishConnection()
                retryCount++
            }
        })
    }

    private fun verifySubPurchase(purchase: Purchase, attemptsLeft: Int = 3) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (purchase.isAcknowledged) {
            PrefUtil.setPremium(this, true)
            PurchaseAnalyticsLogger.logEntitlementActivated(
                productId = purchase.products.firstOrNull(),
                productType = BillingClient.ProductType.SUBS,
                token = purchase.purchaseToken,
                source = "premium_activity"
            )
            return
        }
        PurchaseAnalyticsLogger.logPurchaseAckStarted(
            productId = purchase.products.firstOrNull(),
            token = purchase.purchaseToken,
            attemptsLeft = attemptsLeft,
            source = "premium_activity"
        )

        val acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                PurchaseAnalyticsLogger.logPurchaseAckSuccess(
                    productId = purchase.products.firstOrNull(),
                    token = purchase.purchaseToken,
                    billingResult = billingResult,
                    source = "premium_activity"
                )
                PrefUtil.setPremium(this, true)
                PurchaseAnalyticsLogger.logEntitlementActivated(
                    productId = purchase.products.firstOrNull(),
                    productType = BillingClient.ProductType.SUBS,
                    token = purchase.purchaseToken,
                    source = "premium_activity"
                )
                Toast.makeText(this, "subscribed_successfully", Toast.LENGTH_SHORT).show()
                goNext()
            } else if (attemptsLeft > 1) {
                PurchaseAnalyticsLogger.logPurchaseAckFailed(
                    productId = purchase.products.firstOrNull(),
                    token = purchase.purchaseToken,
                    attemptsLeft = attemptsLeft,
                    billingResult = billingResult,
                    source = "premium_activity"
                )
                Handler(mainLooper).postDelayed({
                    verifySubPurchase(purchase, attemptsLeft - 1)
                }, 2000)
            } else {
                PurchaseAnalyticsLogger.logPurchaseAckFailed(
                    productId = purchase.products.firstOrNull(),
                    token = purchase.purchaseToken,
                    attemptsLeft = attemptsLeft,
                    billingResult = billingResult,
                    source = "premium_activity"
                )
                Timber.d("Acknowledge failed: ${billingResult.debugMessage}")
                Toast.makeText(this, "subscription_failed", Toast.LENGTH_LONG).show()
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
        Timber.d(
            "queryProductDetails requestedProductIds=${listOf(yearly_ad, monthly_ad, weekly_ad)}"
        )

        billingClient!!.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            Timber.d(
                "queryProductDetails resultCode=${billingResult.responseCode} debugMessage=${billingResult.debugMessage} returnedProductIds=${productDetailsResult.productDetailsList.map { it.productId }}"
            )
            productDetailsById.clear()
            productDetailsResult.productDetailsList.forEach {
                Timber.d("getProducts: ${it.productId}")
                productDetailsById[it.productId] = it
            }
            runOnUiThread { refreshPrices() }
        }
    }

    private fun refreshPrices() {
        productDetailsById[yearly_ad]?.let { binding.sixMonthPrice.text = getPrice(it) }
        productDetailsById[monthly_ad]?.let { binding.yearlyPrice.text = getPrice(it) }
        productDetailsById[weekly_ad]?.let { binding.lifetimePrice.text = getPrice(it) }
        updatePurchasedCardsUi()
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

        val builder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)

        val existingToken = currentSubPurchaseToken
        val alreadyOwnsThisProduct = purchasedProductIds.contains(productDetails.productId)
        if (existingToken != null && !alreadyOwnsThisProduct) {
            val replacementMode = chooseReplacementMode(
                from = purchasedProductIds.firstOrNull(),
                to = productDetails.productId
            )
            builder.setSubscriptionUpdateParams(
                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(existingToken)
                    .setSubscriptionReplacementMode(replacementMode)
                    .build()
            )
            Timber.d(
                "Upgrade flow: from=${purchasedProductIds.firstOrNull()} to=${productDetails.productId} mode=$replacementMode"
            )
        }

        val billingResult = billingClient!!.launchBillingFlow(this@PremiumActivity, builder.build())
        PurchaseAnalyticsLogger.logPurchaseFlowLaunched(
            productId = productDetails.productId,
            productType = BillingClient.ProductType.SUBS,
            screen = "premium_activity",
            billingResult = billingResult,
            source = if (isSplash) "splash" else "in_app"
        )
        Timber.d(
            "launchBillingFlow productId=${productDetails.productId} responseCode=${billingResult.responseCode} debugMessage=${billingResult.debugMessage}"
        )
    }

    /** Pick a replacement mode based on whether the user is moving to a higher- or lower-tier plan.
     *  Tier order (cheapest → most expensive): weekly < monthly < yearly. */
    private fun chooseReplacementMode(from: String?, to: String): Int {
        val tier = mapOf(weekly_ad to 1, monthly_ad to 2, yearly_ad to 3)
        val fromTier = tier[from]
            ?: return BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION
        val toTier = tier[to]
            ?: return BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION
        return if (toTier > fromTier) {
            // Upgrade: charge prorated, switch immediately. Best UX for the user.
            BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE
        } else {
            // Downgrade: defer until current period ends so the user keeps what they paid for.
            BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.DEFERRED
        }
    }

    override fun onResume() {
        super.onResume()
        syncPurchasedSubscriptions()
    }

    private fun syncPurchasedSubscriptions() {
        PurchaseAnalyticsLogger.logRestoreStarted(
            productType = BillingClient.ProductType.SUBS,
            source = "premium_activity"
        )
        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            PurchaseAnalyticsLogger.logRestoreResult(
                productType = BillingClient.ProductType.SUBS,
                billingResult = billingResult,
                purchaseCount = list.size,
                source = "premium_activity"
            )
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchasedProductIds.clear()
                currentSubPurchaseToken = null
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        purchasedProductIds.addAll(purchase.products)
                        // Remember an existing active sub so a new buy becomes an upgrade, not a parallel purchase.
                        currentSubPurchaseToken = purchase.purchaseToken
                        verifySubPurchase(purchase)
                    }
                }
                runOnUiThread { updatePurchasedCardsUi() }
            }
        }
    }

    private fun updatePurchasedCardsUi() {
        val yearlyDisabled = isCardDisabledForCurrentOwnership(yearly_ad)
        val monthlyDisabled = isCardDisabledForCurrentOwnership(monthly_ad)
        val weeklyDisabled = isCardDisabledForCurrentOwnership(weekly_ad)

        setCardUiState(binding.sixMonth, binding.sixMonthPrice, yearlyDisabled)
        setCardUiState(binding.yearly, binding.yearlyPrice, monthlyDisabled)
        setCardUiState(binding.lifetime, binding.lifetimePrice, weeklyDisabled)
        updateContinueButtonState(yearlyDisabled, monthlyDisabled, weeklyDisabled)
    }

    private fun setCardUiState(card: View, priceView: android.widget.TextView, disabled: Boolean) {
        card.alpha = if (disabled) 0.45f else 1.0f
        card.isEnabled = !disabled
        card.isClickable = !disabled
        priceView.alpha = if (disabled) 0.7f else 1.0f
    }

    private fun updateContinueButtonState(
        yearlyDisabled: Boolean,
        monthlyDisabled: Boolean,
        weeklyDisabled: Boolean
    ) {
        val allPlansDisabled = yearlyDisabled && monthlyDisabled && weeklyDisabled
        binding.continueBtn.isEnabled = !allPlansDisabled
        binding.continueBtn.isClickable = !allPlansDisabled
        binding.continueBtn.alpha = if (allPlansDisabled) 0.55f else 1.0f
    }

    private fun isCardDisabledForCurrentOwnership(productId: String): Boolean {
        val hasYearly = purchasedProductIds.contains(yearly_ad)
        val hasMonthly = purchasedProductIds.contains(monthly_ad)
        val hasWeekly = purchasedProductIds.contains(weekly_ad)

        return when {
            // If user has yearly, keep all cards disabled.
            hasYearly -> true
            // If user has monthly, disable monthly and weekly cards.
            hasMonthly -> productId == monthly_ad || productId == weekly_ad
            // If user has weekly, keep monthly/yearly enabled for upgrade.
            hasWeekly -> productId == weekly_ad
            else -> false
        }
    }

    private fun showBypassInterstitialAndNavigate(
        screen: String,
        trigger: String,
        nextIntent: Intent
    ) {
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

    private fun logBillingDiagnostics(stage: String) {
        val installer = packageManager.getInstallerPackageName(packageName)
        val accounts = try {
            val am = getSystemService(Context.ACCOUNT_SERVICE) as android.accounts.AccountManager
            am.accounts.map { "${it.name}(${it.type})" }
        } catch (e: Throwable) {
            listOf("unavailable:${e.javaClass.simpleName}")
        }
        Timber.d(
            "stage=$stage packageName=$packageName installer=$installer buildType=${BuildConfig.BUILD_TYPE} versionName=${BuildConfig.VERSION_NAME} versionCode=${BuildConfig.VERSION_CODE} deviceAccounts=$accounts"
        )
    }
}
