package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.MobileAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.AppOpenManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.InterstitialAdsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdShowCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivitySplashBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.PrefsName
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.isFirstTime
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.GoogleMobileAdsConsentManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.GooglePlayBuySubscription
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Splash : BaseActivity() {
    @Inject
    lateinit var adControlConfigManager: AdControlConfigManager

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val TAG = "MySplash"

    private var isAdShowing = false
    private var splashAdAttempted = false
    private val adsPref by lazy { AdsPref(applicationContext) }
    private val appOpenManager by lazy { AppOpenManager.getInstance(application) }
    private val interstitialManager by lazy { InterstitialAdsManager.getInstance(adsPref) }
    private var splashDurationMs = 12000L

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

        if (PrefUtil.isPremium(this)) {
            splashDurationMs = 1200L
        }

        loadProgress()
        GooglePlayBuySubscription.initBillingClient(this)
        GooglePlayBuySubscription.makeGooglePlayConnectionRequest()
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

        trySplashAdsWithPriority()

        CoroutineScope(Dispatchers.Main).launch {
            delay(splashDurationMs)
            if (!isAdShowing) {
                Log.e(TAG, "12 sec timeout — navigating")
                startMainActivity("timeout")
            } else {
                Log.d(TAG, "12 sec timeout — ad is showing, waiting for user")
            }
        }
    }

    private fun trySplashAdsWithPriority() {
        if (splashAdAttempted || isNext || PrefUtil.isPremium(this)) return
        splashAdAttempted = true

        val appOpenId = if (BuildConfig.DEBUG) {
            ADS.TEST_ADMOB_APP_OPEN_SPLASH_ID
        } else {
            ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID
        }
        appOpenManager.updateCurrentActivity(this)
        appOpenManager.loadAppOpenAd(
            adUnitId = appOpenId,
            adsPref = adsPref,
            onAdLoaded = {
                // Block the 8-sec timeout immediately on load success — onAdShown fires
                // only after the ad is fully presented, which can race past the timeout.
                isAdShowing = true
                appOpenManager.showAppOpenAdIfAvailable(adsPref, object : AdShowCallback {
                    override fun onAdShown() {
                        isAdShowing = true
                    }

                    override fun onAdFailedToShow(adError: String) {
                        showSplashInterstitialFallback()
                    }

                    override fun onAdDismissed() {
                        isAdShowing = false
                        startMainActivity("app_open_dismissed")
                    }
                })
            },
            onAdFailed = {
                showSplashInterstitialFallback()
            }
        )
    }

    private fun showSplashInterstitialFallback() {
        if (isNext || PrefUtil.isPremium(this)) return
        isAdShowing = true  // Block the 8-sec timeout while the interstitial loads/shows
        val interstitialId = if (BuildConfig.DEBUG) {
            ADS.TEST_ADMOB_INTERSTITIAL_SPLASH_AD_ID
        } else {
            ADS.PROD_ADMOB_INTERSTITIAL_SPLASH_AD_ID
        }
        interstitialManager.loadInterstitialAd(
            activity = this,
            interstitialAdUnitId = interstitialId,
            onAdLoaded = {
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

    private fun startMainActivity(s: String) {
        if (!isNext) {
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
    }

    companion object {
        var isAlReadyShow = false
        const val TAG = "SplashActivity"
    }
}
