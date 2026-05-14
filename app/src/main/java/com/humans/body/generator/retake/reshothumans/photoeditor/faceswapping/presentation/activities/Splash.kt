package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.MobileAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app.BillingManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.NativeAdConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.RemoteScreens
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdsManagerEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.AdConfigInitializer
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdEvent
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.AppOpenManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.InterstitialAdsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdShowCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.update.ImmediateAppUpdater
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.FunnelAnalytics
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivitySplashBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.PrefsName
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.isFirstTime
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.GoogleMobileAdsConsentManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class Splash : BaseActivity() {
    @Inject
    lateinit var adControlConfigManager: AdControlConfigManager
    @Inject
    lateinit var billingManager: BillingManager
    @Inject
    lateinit var adConfigInitializer: AdConfigInitializer
    @Inject
    lateinit var nativeAdConfigManager: NativeAdConfigManager

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val TAG = "MySplash"

    private var isAdShowing = false
    private var awaitingAppOpenDismiss = false
    private var appOpenShowRequested = false
    private var splashAdAttempted = false
    private var splashNativeResolved = false
    private var hasConfiguredSplashNative = false
    private var hasEnabledSplashNative = false
    private var splashAppOpenReady = false
    private var splashAppOpenFailed = false
    private var remoteConfigReady = false
    private var splashPreparationStarted = false
    private var topNativeResolved = false
    private var bottomNativeResolved = false
    private var splashTimeoutReached = false
    private val adsPref by lazy { AdsPref(applicationContext) }
    private val appOpenManager by lazy { AppOpenManager.getInstance(application) }
    private val interstitialManager by lazy { InterstitialAdsManager.getInstance(adsPref) }
    private var splashDurationMs = 12000L
    private val splashAppOpenWaitMs = 6000L
    private lateinit var immediateAppUpdater: ImmediateAppUpdater
    private var updateFlowActive = false
    private var consentFlowStarted = false
    private val immediateUpdateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // If update is canceled or fails, continue normal app flow.
            updateFlowActive = false
            startConsentFlowOnce()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        refreshSplashNativeModeFromConfig()
        applySplashLoadingUiMode()

        binding.getStart.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "splash", "click_get_started")
            binding.getStart.isEnabled = false
            binding.getStart.alpha = 0.7f
            trySplashAdsWithPriority()
        }
        adConfigInitializer.setListener(
            onReady = {
                remoteConfigReady = true
                refreshSplashNativeModeFromConfig()
                applySplashLoadingUiMode()
                maybePrepareSplashAds()
            },
            onFailed = {
                remoteConfigReady = true
                refreshSplashNativeModeFromConfig()
                applySplashLoadingUiMode()
                maybePrepareSplashAds()
            }
        )
        lifecycleScope.launch {
            delay(4000L)
            if (!remoteConfigReady) {
                Log.w(TAG, "Remote config not ready in time for splash; using fallback ad prep")
                remoteConfigReady = true
                maybePrepareSplashAds()
            }
        }

        immediateAppUpdater = ImmediateAppUpdater(this)

        // Defer the fast-path commit until billing has reconciled. A cached-premium user
        // whose subscription has actually expired must not collapse to the 1.2s splash,
        // otherwise the splash app-open ad finishes loading after we navigate and lands
        // on top of the next screen (e.g. LanguagesActivity).
        lifecycleScope.launch {
            if (PrefUtil.isPremium(this@Splash)) {
                withTimeoutOrNull(2000) { billingManager.firstSyncCompleted.first { it } }
            }
            splashDurationMs = if (PrefUtil.isPremium(this@Splash)) 1200L else 12000L
            loadProgress()
            checkImmediateUpdateThenContinue()
        }

        FunnelAnalytics.logScreenEvent(this, "splash", "on_create")
    }

    override fun onDestroy() {
        FunnelAnalytics.logScreenEvent(this, "splash", "on_destroy")
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (updateFlowActive) {
            runCatching {
                immediateAppUpdater.resumeIfImmediateUpdateInProgress(
                    launcher = immediateUpdateLauncher,
                    onUpdateFlowLaunched = { updateFlowActive = true }
                )
            }.onFailure {
                Log.w(TAG, "Update resume failed unexpectedly", it)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Fallback only when Splash regains focus (ad overlay gone) and we were waiting
        // for app-open dismissal. This avoids navigating while ad is still visible.
        if (hasFocus && awaitingAppOpenDismiss && !isNext) {
            binding.root.postDelayed({
                if (hasWindowFocus() && awaitingAppOpenDismiss && !isNext) {
                    awaitingAppOpenDismiss = false
                    isAdShowing = false
                    startMainActivity("app_open_focus_fallback", waitForBillingSync = false)
                }
            }, 350L)
        }
    }

    private fun loadSplashNatives() {
        refreshSplashNativeModeFromConfig()
        applySplashLoadingUiMode()
        val nativeTopContainer = binding.llNativeTop.findViewById<FrameLayout>(R.id.admob_native)
        val nativeTopShimmer = binding.llNativeTop.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer = binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)

        lifecycleScope.launch {
            entryPoint.adsManager().loadNativeAds(
                this@Splash,
                RemoteScreens.SPLASH_SCREEN,
                listOf(
                    NativeAdConfig("top", nativeTopContainer, nativeTopShimmer),
                    NativeAdConfig("bottom", nativeBottomContainer, nativeBottomShimmer)
                )
            ) { event ->
                when (event) {
                    is NativeAdEvent.Loaded -> {
                        FunnelAnalytics.logScreenEvent(this@Splash, "splash", "on_native_loaded")
                        if (event.position.equals("top", ignoreCase = true)) topNativeResolved = true
                        if (event.position.equals("bottom", ignoreCase = true)) bottomNativeResolved = true
                        hasEnabledSplashNative = true
                        splashNativeResolved = true
                        applySplashLoadingUiMode()
                        maybeContinueAfterSplashAssets()
                        onSplashNativeResolutionChanged()
                    }
                    is NativeAdEvent.Failed -> {
                        FunnelAnalytics.logScreenEvent(this@Splash, "splash", "on_native_failed")
                        if (event.position.equals("top", ignoreCase = true)) topNativeResolved = true
                        if (event.position.equals("bottom", ignoreCase = true)) bottomNativeResolved = true
                        onSplashNativeResolutionChanged()
                    }
                    is NativeAdEvent.Off -> {
                        if (event.position.equals("top", ignoreCase = true)) topNativeResolved = true
                        if (event.position.equals("bottom", ignoreCase = true)) bottomNativeResolved = true
                        onSplashNativeResolutionChanged()
                    }
                    is NativeAdEvent.AllOffFromConfig -> {
                        topNativeResolved = true
                        bottomNativeResolved = true
                        onSplashNativeResolutionChanged()
                    }
                }
            }
        }
    }

    private fun onSplashNativeResolutionChanged() {
        if (splashNativeResolved) return
        if (!topNativeResolved || !bottomNativeResolved) return
        splashNativeResolved = true
        applySplashLoadingUiMode()
        maybeContinueAfterSplashAssets()
    }

    private fun shouldWaitForGetStartedTap(): Boolean {
        return !PrefUtil.isPremium(this) &&
            hasEnabledSplashNative &&
            adControlConfigManager.shouldShowAppOpenSplash()
    }

    private fun showGetStartedButton() {
        if (isNext) return
        binding.verticalProgress.visibility = View.GONE
        binding.circularProgress.visibility = View.GONE
        binding.textView1.visibility = View.VISIBLE
        binding.getStart.visibility = View.VISIBLE
        binding.getStart.isEnabled = true
        binding.getStart.alpha = 1f
    }

    private fun maybeShowGetStartedButton() {
        if (!shouldWaitForGetStartedTap()) return
        if (!splashNativeResolved) return
        if (!splashAppOpenReady) return
        showGetStartedButton()
    }

    private fun maybeContinueAfterSplashAssets() {
        if (isNext) return
        if (!splashNativeResolved || !splashAppOpenReady) return

        if (hasEnabledSplashNative) {
            maybeShowGetStartedButton()
        } else {
            trySplashAdsWithPriority()
        }
    }

    private fun applySplashLoadingUiMode() {
        if (hasConfiguredSplashNative || hasEnabledSplashNative) {
            binding.verticalProgress.visibility = View.GONE
            binding.circularProgress.visibility = View.VISIBLE
            binding.textView1.visibility = View.GONE
            binding.getStart.visibility = View.GONE
        } else {
            binding.circularProgress.visibility = View.GONE
            binding.verticalProgress.visibility = View.VISIBLE
            binding.textView1.visibility = View.VISIBLE
            binding.getStart.visibility = View.GONE
        }
    }

    private fun refreshSplashNativeModeFromConfig() {
        hasConfiguredSplashNative =
            nativeAdConfigManager.isNativeVisible(RemoteScreens.SPLASH_SCREEN, "top") ||
            nativeAdConfigManager.isNativeVisible(RemoteScreens.SPLASH_SCREEN, "bottom")
    }

    private fun maybePrepareSplashAds() {
        if (splashPreparationStarted || !remoteConfigReady || !isMobileAdsInitializeCalled.get()) return
        splashPreparationStarted = true
        refreshSplashNativeModeFromConfig()
        applySplashLoadingUiMode()
        loadSplashNatives()
        preloadSplashAppOpenForGate()
    }

    private fun checkImmediateUpdateThenContinue() {
        runCatching {
            immediateAppUpdater.checkAndLaunchImmediateUpdate(
                launcher = immediateUpdateLauncher,
                onUpdateFlowLaunched = {
                    updateFlowActive = true
                },
                onNoImmediateUpdate = {
                    updateFlowActive = false
                    startConsentFlowOnce()
                }
            )
        }.onFailure {
            Log.w(TAG, "Immediate update check crashed unexpectedly", it)
            updateFlowActive = false
            startConsentFlowOnce()
        }
    }

    private fun startConsentFlowOnce() {
        if (consentFlowStarted) return
        consentFlowStarted = true
        // Billing is initialized centrally via Hilt BillingManager in MyApplication.onCreate().
        requestConsent()
    }

    private fun loadProgress() {
        val max = binding.verticalProgress.max
        val animator = ObjectAnimator.ofInt(binding.verticalProgress, "progress", 0, max)
        animator.duration = splashDurationMs
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun requestConsent() {
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                Log.w(TAG, "${consentError.errorCode}: ${consentError.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    delay(splashDurationMs)
                    if (!isAdShowing) startMainActivity("consent_error_timeout")
                }
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdkOther()
            }
            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                invalidateOptionsMenu()
            }
        }

        if (googleMobileAdsConsentManager.canRequestAds) {
            initializeMobileAdsSdkOther()
        }
    }

    private fun initializeMobileAdsSdkOther() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) return
        MobileAds.initialize(this) {}
        maybePrepareSplashAds()

        CoroutineScope(Dispatchers.Main).launch {
            delay(splashDurationMs)
            splashTimeoutReached = true
            if (PrefUtil.isPremium(this@Splash)) {
                startMainActivity("premium_timeout")
                return@launch
            }
            if (!splashNativeResolved) {
                maybePrepareSplashAds()
                trySplashAdsWithPriority()
                return@launch
            }
            if (shouldWaitForGetStartedTap()) {
                maybeShowGetStartedButton()
                if (splashAppOpenFailed) {
                    startMainActivity("timeout_splash_app_open_failed")
                }
                return@launch
            }
            trySplashAdsWithPriority()
            Log.d(TAG, "12 sec timeout — splash ad pipeline triggered")
        }
    }

    private fun preloadSplashAppOpenForGate() {
        if (PrefUtil.isPremium(this)) return
        if (!adControlConfigManager.shouldShowAppOpenSplash()) {
            splashAppOpenFailed = true
            return
        }

        val appOpenId = getSplashAppOpenAdUnitId()
        appOpenManager.updateCurrentActivity(this)

        if (appOpenManager.hasUsableAd(appOpenId)) {
            splashAppOpenReady = true
            maybeContinueAfterSplashAssets()
            return
        }

        appOpenManager.loadAppOpenAd(
            adUnitId = appOpenId,
            adsPref = adsPref,
            onAdLoaded = {
                if (isNext) return@loadAppOpenAd null
                splashAppOpenReady = true
                splashAppOpenFailed = false
                FunnelAnalytics.logScreenEvent(this@Splash, "splash", "on_app_open_loaded")
                maybeContinueAfterSplashAssets()
            },
            onAdFailed = {
                splashAppOpenFailed = true
                FunnelAnalytics.logScreenEvent(this@Splash, "splash", "on_app_open_failed")
                if (splashTimeoutReached && shouldWaitForGetStartedTap()) {
                    startMainActivity("splash_app_open_preload_failed")
                }
            }
        )
    }

    private fun trySplashAdsWithPriority() {
        if (splashAdAttempted || isNext || PrefUtil.isPremium(this)) return
        splashAdAttempted = true

        if (!adControlConfigManager.shouldShowAppOpenSplash()) {
            startMainActivity("splash_app_open_disabled")
            return
        }

        val appOpenId = getSplashAppOpenAdUnitId()
        appOpenManager.updateCurrentActivity(this)

        if (appOpenManager.hasUsableAd(appOpenId)) {
            showSplashAppOpenOrFallback()
            return
        }

        if (appOpenManager.isAdLoading(appOpenId)) {
            lifecycleScope.launch {
                val checks = (splashAppOpenWaitMs / 250L).toInt()
                repeat(checks) {
                    if (isNext) return@launch
                    delay(250)
                    if (appOpenManager.hasUsableAd(appOpenId)) {
                        showSplashAppOpenOrFallback()
                        return@launch
                    }
                }
                showSplashInterstitialFallback()
            }
            return
        }

        appOpenManager.loadAppOpenAd(
            adUnitId = appOpenId,
            adsPref = adsPref,
            onAdLoaded = {
                if (isNext) {
                    Log.d(TAG, "splash app-open loaded after navigation — dropping")
                    return@loadAppOpenAd null
                }
                FunnelAnalytics.logScreenEvent(this@Splash, "splash", "on_app_open_loaded")
                showSplashAppOpenOrFallback()
            },
            onAdFailed = {
                FunnelAnalytics.logScreenEvent(this@Splash, "splash", "on_app_open_failed")
                showSplashInterstitialFallback()
            }
        )
    }

    private fun getSplashAppOpenAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            ADS.TEST_ADMOB_APP_OPEN_SPLASH_ID
        } else {
            adControlConfigManager.getProdAppOpenSplashAdUnitId(ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID)
        }
    }

    private fun showSplashAppOpenOrFallback() {
        if (appOpenShowRequested || isNext) return
        appOpenShowRequested = true

        // Do not mark as showing until SDK confirms onAdShown().
        appOpenManager.showAppOpenAdIfAvailable(this, adsPref, object : AdShowCallback {
            override fun onAdShown() {
                isAdShowing = true
                awaitingAppOpenDismiss = true
            }

            override fun onAdFailedToShow(adError: String) {
                // Duplicate show attempts can report "already showing" while the app-open
                // is currently on screen. That's not a true failure and must not trigger
                // interstitial fallback on top.
                if (
                    adError.contains("already showing", ignoreCase = true) ||
                    adError.contains("in progress", ignoreCase = true)
                ) {
                    return
                }
                appOpenShowRequested = false
                isAdShowing = false
                startMainActivity("app_open_failed_to_show")
            }

            override fun onAdDismissed() {
                appOpenShowRequested = false
                awaitingAppOpenDismiss = false
                isAdShowing = false
                startMainActivity("app_open_dismissed", waitForBillingSync = false)
            }
        })
    }

    private fun showSplashInterstitialFallback() {
        if (
            isNext ||
            PrefUtil.isPremium(this) ||
            appOpenShowRequested ||
            awaitingAppOpenDismiss ||
            appOpenManager.isShowingOrShowingSoon()
        ) return
        isAdShowing = true  // Block the 8-sec timeout while the interstitial loads/shows
        val interstitialId = if (BuildConfig.DEBUG) {
            ADS.TEST_ADMOB_INTERSTITIAL_SPLASH_AD_ID
        } else {
            adControlConfigManager.getProdInterstitialAdUnitId(
                RemoteScreens.SPLASH_SCREEN,
                ADS.PROD_ADMOB_INTERSTITIAL_SPLASH_AD_ID
            )
        }
        interstitialManager.loadInterstitialAd(
            activity = this,
            interstitialAdUnitId = interstitialId,
            onAdLoaded = {
                if (isNext) {
                    Log.d(TAG, "splash interstitial loaded after navigation — dropping")
                    return@loadInterstitialAd null
                }
                interstitialManager.showInterstitialAd(
                    activity = this,
                    onAdFailedToShow = {
                        isAdShowing = false
                        startMainActivity("splash_interstitial_failed_to_show")
                    },
                    onAdDismissed = {
                        isAdShowing = false
                        startMainActivity("splash_interstitial_dismissed")
                    }
                )
            },
            onAdFailedToLoad = {
                isAdShowing = false
                startMainActivity("splash_interstitial_failed_to_load")
            }
        )
    }

    private var isNext = false

    private fun startMainActivity(s: String, waitForBillingSync: Boolean = true) {
        if (isNext) return
        if (!waitForBillingSync) {
            performNavigation(s)
            return
        }
        // Ensure BillingManager has reconciled with Google Play before we route — otherwise
        // an expired-but-still-cached premium flag would skip ads on this session.
        lifecycleScope.launch {
            withTimeoutOrNull(3000) { billingManager.firstSyncCompleted.first { it } }
            performNavigation(s)
        }
    }

    private fun performNavigation(s: String) {
        if (isNext) return
        isNext = true
        binding.verticalProgress.visibility = View.GONE
        val prefs = getSharedPreferences(PrefsName, MODE_PRIVATE)
        val isFirstLaunch = !prefs.getBoolean(isFirstTime, false)

        val nextIntent = when {
            isFirstLaunch && adControlConfigManager.shouldShowLanguagesFirst() -> {
                Intent(this@Splash, LanguagesActivity::class.java).putExtra("isSplash", true)
            }

            !isFirstLaunch && adControlConfigManager.shouldShowLanguagesSecond() -> {
                Intent(this@Splash, LanguagesActivity::class.java).putExtra("isSplash", true)
            }

            isFirstLaunch && adControlConfigManager.shouldShowIntroFirst() -> {
                Intent(this@Splash, OnBoardingActivity::class.java)
            }

            !isFirstLaunch && adControlConfigManager.shouldShowIntroSecond() -> {
                Intent(this@Splash, OnBoardingActivity::class.java)
            }

            isFirstLaunch && adControlConfigManager.shouldShowPremiumFirst() && !PrefUtil.isPremium(this) -> {
                prefs.edit().putBoolean(isFirstTime, true).apply()
                Intent(this@Splash, PremiumActivity::class.java)
                    .putExtra("isSplash", true)
                    .putExtra("isFirstPremiumFlow", true)
            }

            !isFirstLaunch && adControlConfigManager.shouldShowPremiumSecond() && !PrefUtil.isPremium(this) -> {
                Intent(this@Splash, PremiumActivity::class.java)
                    .putExtra("isSplash", true)
                    .putExtra("isFirstPremiumFlow", false)
            }

            else -> {
                if (isFirstLaunch) {
                    prefs.edit().putBoolean(isFirstTime, true).apply()
                }
                Intent(this@Splash, HomeActivity::class.java)
            }
        }

        startActivity(nextIntent)
        Log.d(TAG, "goNextActivity: $s")
        finish()
    }

    companion object {
        var isAlReadyShow = false
        const val TAG = "SplashActivity"
    }
}
