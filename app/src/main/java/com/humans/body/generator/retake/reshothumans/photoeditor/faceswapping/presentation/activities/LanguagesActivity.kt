package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.LangAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.RemoteScreens
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdsManagerEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.BannerConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdEvent
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivityLanguagesBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.PrefsName
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.isFirstTime
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getLangData
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.FunnelAnalytics
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class LanguagesActivity : BaseActivity() {
    @Inject
    lateinit var adControlConfigManager: AdControlConfigManager

    private val binding by lazy {
        ActivityLanguagesBinding.inflate(layoutInflater)
    }
    var pos = 0
    private var isSplash = false
    private var isBottomNativeResolved = false
    private var nativeGateTimeoutJob: Job? = null
    private var applyPendingUntilNative = false
    private var applyRequested = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.black) // or your dark color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        Log.d(
            "TAG",
            "onCreate: ${SharePref.getInt(Constants.native_language_width, 0)}, ${
                SharePref.getInt(
                    Constants.native_language_height,
                    48
                )
            }"
        )

        pos = getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("lang", 0)

        isSplash = intent.getBooleanExtra("isSplash", false)
        if (!isSplash)
            binding.back.visibility = View.VISIBLE
        else binding.back.visibility = View.GONE
        binding.back.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "language", "click_back")
            finish()
        }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        val adapter = LangAdapter(getLangData()) { it, it1 ->
            pos = it1

        }
        binding.recycler.adapter = adapter
        adapter.setPos(getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("lang", 0))
        loadTopBanner()
        startBottomNativeGate()
        loadBottomNative()

        binding.done.setOnClickListener {
            FunnelAnalytics.logScreenEvent(this, "language", "click_done")
            applyRequested = true
            getSharedPreferences("MySharedPref", MODE_PRIVATE).edit { putInt("lang", pos) }
            Log.d("TAG", "onCreate: ${getLangData()[pos].locale}")
            // Calling updateLocale() here can recreate this Activity and interrupt ad loading flow.
            // For splash flow, defer locale apply to avoid interstitial loading dialog flash/miss.
            if (!isSplash) {
                updateLocale(getLangData()[pos].locale)
            }
            if (isBottomNativeResolved) {
                goNext()
            } else {
                applyPendingUntilNative = true
                setDoneLoadingState(isLoading = true)
            }


        }

        FunnelAnalytics.logScreenEvent(this, "language", "on_create")
    }

    override fun onDestroy() {
        FunnelAnalytics.logScreenEvent(this, "language", "on_destroy")
        super.onDestroy()
    }

    private fun loadTopBanner() {
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)
        lifecycleScope.launch {
            entryPoint.adsManager().loadBannerAds(
                this@LanguagesActivity,
                RemoteScreens.LANGUAGE_SCREEN,
                listOf(BannerConfig("top", bannerTopContainer, bannerTopShimmer))
            )
        }
    }

    private fun loadBottomNative() {
        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer = binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)
        lifecycleScope.launch {
            entryPoint.adsManager().loadNativeAds(
                this@LanguagesActivity,
                RemoteScreens.LANGUAGE_SCREEN,
                listOf(NativeAdConfig("bottom", nativeBottomContainer, nativeBottomShimmer))
            ) { event ->
                when (event) {
                    is NativeAdEvent.Loaded -> {
                        if (event.position.equals("bottom", ignoreCase = true)) onBottomNativeResolved("loaded")
                        FunnelAnalytics.logScreenEvent(this@LanguagesActivity, "language", "on_native_loaded")
                    }
                    is NativeAdEvent.Failed -> {
                        if (event.position.equals("bottom", ignoreCase = true)) onBottomNativeResolved("failed")
                        FunnelAnalytics.logScreenEvent(this@LanguagesActivity, "language", "on_native_failed")
                    }
                    is NativeAdEvent.Off -> {
                        if (event.position.equals("bottom", ignoreCase = true)) onBottomNativeResolved("off")
                    }
                    is NativeAdEvent.AllOffFromConfig -> onBottomNativeResolved("all_off")
                }
            }
        }
    }

    private fun startBottomNativeGate() {
        setDoneLoadingState(isLoading = true)
        nativeGateTimeoutJob?.cancel()
        nativeGateTimeoutJob = lifecycleScope.launch {
            delay(4000L)
            if (!isBottomNativeResolved) {
                Log.d("LanguageNativeTrace", "native timeout reached; unlocking apply")
                onBottomNativeResolved("timeout")
            }
        }
    }

    private fun onBottomNativeResolved(reason: String) {
        if (isBottomNativeResolved) return
        Log.d("LanguageNativeTrace", "bottom native resolved: $reason")
        isBottomNativeResolved = true
        nativeGateTimeoutJob?.cancel()
        setDoneLoadingState(isLoading = false)
        if (applyPendingUntilNative && applyRequested) {
            applyPendingUntilNative = false
            goNext()
        }
    }

    private fun setDoneLoadingState(isLoading: Boolean) {
        binding.done.isEnabled = !isLoading
        binding.done.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.doneProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun goNext() {
        if (isSplash) {
            val prefs = getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
            val isFirstLaunch = !prefs.getBoolean(isFirstTime, false)
            val nextIntent = when {
                isFirstLaunch && adControlConfigManager.shouldShowIntroFirst() -> {
                    Intent(this, OnBoardingActivity::class.java)
                }

                !isFirstLaunch && adControlConfigManager.shouldShowIntroSecond() -> {
                    Intent(this, OnBoardingActivity::class.java)
                }

                isFirstLaunch && adControlConfigManager.shouldShowPremiumFirst() && !com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil.isPremium(this) -> {
                    prefs.edit { putBoolean(isFirstTime, true) }
                    Intent(this, PremiumActivity::class.java)
                        .putExtra("isSplash", true)
                        .putExtra("isFirstPremiumFlow", true)
                }

                !isFirstLaunch && adControlConfigManager.shouldShowPremiumSecond() && !com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil.isPremium(this) -> {
                    Intent(this, PremiumActivity::class.java)
                        .putExtra("isSplash", true)
                        .putExtra("isFirstPremiumFlow", false)
                }

                else -> {
                    if (isFirstLaunch) prefs.edit { putBoolean(isFirstTime, true) }
                    Intent(this, HomeActivity::class.java)
                }
            }
            showBypassInterstitialAndNavigate(RemoteScreens.LANGUAGE_SCREEN, "continue", nextIntent)


        } else finish()
    }

    private fun showBypassInterstitialAndNavigate(screen: String, trigger: String, nextIntent: Intent) {
        if (isFinishing || isDestroyed) return
        // Premium users do not see interstitials, so navigate directly and avoid hiding
        // the current UI first (which can otherwise flash a black screen).
        if (com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil.isPremium(this)) {
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(nextIntent)
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        // Hide the language content immediately. The interstitial covers the screen, but on
        // dismissal the activity briefly resumes before finish takes effect — without this,
        // the language UI re-renders for a frame between the ad closing and the next activity.
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        binding.recycler.visibility = View.INVISIBLE
        binding.done.visibility = View.INVISIBLE
        binding.doneProgress.visibility = View.INVISIBLE
        binding.back.visibility = View.INVISIBLE
        binding.llBannerTop.visibility = View.INVISIBLE
        binding.llNativeBottom.visibility = View.INVISIBLE
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)
        lifecycleScope.launch {
            entryPoint.adsManager().showInterstitialAd(
                activity = this@LanguagesActivity,
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
