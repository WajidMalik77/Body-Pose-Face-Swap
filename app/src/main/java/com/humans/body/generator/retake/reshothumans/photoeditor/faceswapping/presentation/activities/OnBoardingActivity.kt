package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
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
import androidx.viewpager2.widget.ViewPager2
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.OnboardingAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.NativeAdConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.RemoteScreens
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdsManagerEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.ActivityNative
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.BannerConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.AdmobNativeManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivityOnBoardingBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.LayoutFullscreenAdIntroBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.PrefsName
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.isFirstTime
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.BillingUtilsIAP
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.OnboardingItem
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_AD
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_DATA
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {
    @Inject
    lateinit var adControlConfigManager: AdControlConfigManager
    @Inject
    @ActivityNative
    lateinit var admobNativeManager: AdmobNativeManager
    @Inject
    lateinit var nativeAdConfigManager: NativeAdConfigManager

    private val binding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    companion object {
        internal var isFullAd = false

    }

    private lateinit var adapter: OnboardingAdapter
    private var isIntroFullNativeLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.black) // or your dark color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        loadOnboardingAds()
        setViewPager()
        binding.nextBtn.setOnClickListener {
            val current = binding.viewPager.currentItem
            val total = binding.viewPager.adapter?.itemCount ?: 0

            if (current < total - 1) {
                // Go to next page
                binding.viewPager.setCurrentItem(current + 1, true)
            } else {
                goNext()
            }
        }

        binding.nextBtn.paintFlags = binding.nextBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateOnboardingChromeForPage(position)
                if (position == pages.size - 1) {
                    binding.nextBtn.text = getString(R.string.finish)
                } else {
                    binding.nextBtn.text = getString(R.string.next)
                }
                binding.intoTabLayout.selectIndicator(position)
            }
        })
    }

    private fun loadOnboardingAds() {
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer = binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        lifecycleScope.launch {
            entryPoint.adsManager().loadBannerAds(
                this@OnBoardingActivity,
                RemoteScreens.INTRO_SCREEN,
                listOf(BannerConfig("top", bannerTopContainer, bannerTopShimmer))
            )
            entryPoint.adsManager().loadNativeAds(
                this@OnBoardingActivity,
                RemoteScreens.INTRO_SCREEN,
                listOf(NativeAdConfig("bottom", nativeBottomContainer, nativeBottomShimmer))
            )
        }
    }

    private fun goNext() {
        val prefs = getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
        val wasFirstLaunch = !prefs.getBoolean(isFirstTime, false)
        prefs.edit {
            putBoolean(isFirstTime, true)
        }

        val shouldShowPremium = if (wasFirstLaunch) {
            adControlConfigManager.shouldShowPremiumFirst()
        } else {
            adControlConfigManager.shouldShowPremiumSecond()
        }

        val nextIntent = if (BillingUtilsIAP.isPremium() || !shouldShowPremium) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, PremiumActivity::class.java)
                .putExtra("isSplash", true)
                .putExtra("isFirstPremiumFlow", wasFirstLaunch)
        }
        showBypassInterstitialAndNavigate(RemoteScreens.INTRO_SCREEN, "next", nextIntent)
    }

    val pages = mutableListOf<OnboardingItem>()

    private fun setViewPager() {
        if (!isFinishing) {

// Page 1
            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.replicate_body_pose),
                    description = "",
                    imageRes = R.drawable.ic_onboarding
                )
            )
// Page 2
            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.turn_your_photo_to_ghibli_style_selfie_for_social_media),
                    description = "",
                    imageRes = R.drawable.ic_image_onboarding_1
                )
            )

            // Full-screen intro native appears after the second swipe (after page 2)
            if (!PrefUtil.isPremium(this) && shouldShowIntroFullScreenNative()) {
                pages.add(
                    OnboardingItem(
                        type = TYPE_AD
                    )
                )
            }

// Page 3
            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.easily_swap_your_face_into_any_photo),
                    description = "",
                    imageRes = R.drawable.ic_onboarding_2
                )
            )
            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.edit_your_photo_with_effects_restyle_and_more),
                    description = "",
                    imageRes = R.drawable.ic_onboarding_3
                )
            )

            adapter = OnboardingAdapter(pages) { adBinding ->
                loadIntroFullScreenNative(adBinding)
            }
            binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            binding.viewPager.adapter = adapter
            binding.viewPager.isSaveEnabled = false
            binding.viewPager.currentItem = 0
            binding.intoTabLayout.setupIndicators(adapter.itemCount)

            // Set the first indicator as active
            binding.intoTabLayout.selectIndicator(0)
            updateOnboardingChromeForPage(0)
        }
    }

    private fun updateOnboardingChromeForPage(position: Int) {
        val isAdPage = pages.getOrNull(position)?.type == TYPE_AD
        binding.nextBtn.visibility = if (isAdPage) View.GONE else View.VISIBLE
        binding.llNativeBottom.visibility = if (isAdPage) View.GONE else View.VISIBLE
        binding.intoTabLayout.visibility = if (isAdPage) View.GONE else View.VISIBLE
    }

    private fun shouldShowIntroFullScreenNative(): Boolean {
        val configVisible = nativeAdConfigManager.isNativeVisible(RemoteScreens.INTRO_SCREEN, "full_screen")
        val fallbackVisible = SharePref.getBoolean(Constants.is_native_onboarding_full, true)
        return configVisible || fallbackVisible
    }

    private fun loadIntroFullScreenNative(adBinding: LayoutFullscreenAdIntroBinding) {
        if (!shouldShowIntroFullScreenNative()) return
        if (isIntroFullNativeLoaded) return
        val adUnitId = SharePref.getString(Constants.native_onboarding_ful_key, "")
            .takeIf { it.isNotEmpty() }
            ?: if (BuildConfig.DEBUG) ADS.TEST_ADMOB_NATIVE_ONBOARDING_AD_ID else ADS.PROD_ADMOB_NATIVE_ONBOARDING_AD_ID
        admobNativeManager.loadNativeFullScreenIntroAd(
            adContainer = adBinding.admobNativeFullScreenIntro,
            adUnitId = adUnitId,
            shimmerContainer = adBinding.shimmerContainer,
            shouldPreloadNext = false,
            onLoaded = {
                isIntroFullNativeLoaded = true
                adBinding.shimmerContainer.stopShimmer()
                adBinding.shimmerContainer.visibility = View.GONE
                adBinding.llSwipeToContinue.visibility = View.VISIBLE
            },
            onFailed = {
                adBinding.shimmerContainer.stopShimmer()
                adBinding.shimmerContainer.visibility = View.GONE
                adBinding.llSwipeToContinue.visibility = View.GONE
            }
        )
    }

    private fun showBypassInterstitialAndNavigate(screen: String, trigger: String, nextIntent: Intent) {
        if (isFinishing || isDestroyed) return
        // Hide the intro content immediately. The interstitial covers the screen, but on
        // dismissal the activity briefly resumes before finish takes effect — without this,
        // the intro re-renders for a frame between the ad closing and the next activity.
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        binding.viewPager.visibility = View.INVISIBLE
        binding.nextBtn.visibility = View.INVISIBLE
        binding.intoTabLayout.visibility = View.INVISIBLE
        binding.llBannerTop.visibility = View.INVISIBLE
        binding.llNativeBottom.visibility = View.INVISIBLE
        val entryPoint = EntryPointAccessors.fromActivity(this, AdsManagerEntryPoint::class.java)
        lifecycleScope.launch {
            entryPoint.adsManager().showInterstitialAd(
                activity = this@OnBoardingActivity,
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
