package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers


import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdsManagerEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.BannerConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdEvent
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui.AdsManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Get AdsManager instance for Fragment
 * Cached to avoid repeated EntryPoint lookups
 */
import java.util.WeakHashMap

private val adsManagerCache = WeakHashMap<Activity, AdsManager>()
private const val TAG_INTER = "InterstitialTrace"
private val PRE_HOME_COUNTER_BYPASS_SCREENS = setOf(
    "SplashScreen",
    "LanguagesScreen",
    "IntroScreen",
    "PremiumScreen"
)

private val Fragment.cachedAdsManager: AdsManager?
    get() {
        if (!isAdded) return null
        val act = activity ?: return null

        return adsManagerCache.getOrPut(act) {
            try {
                val entryPoint = EntryPointAccessors.fromActivity(
                    act,
                    AdsManagerEntryPoint::class.java
                )
                entryPoint.adsManager()
            } catch (e: Exception) {
                Timber.e(e, "Failed to get AdsManager for activity=${act::class.java.name}")
                null
            }
        }
    }

/**
 * Initialize ads in Fragment
 */
fun Fragment.initializeAds(screen: String, trigger: String) {
    val activity = activity ?: return
    cachedAdsManager?.initializeAds(activity, screen, trigger)
}

/**
 * Initialize ads without trigger
 */
fun Fragment.initializeAdsSimple(
    onLoaded: () -> Unit = {},
    onFailed: () -> Unit = {}
) {
    val activity = activity ?: return
    cachedAdsManager?.initializeAdsSimple(activity, onLoaded, onFailed)
}

/**
 * Load banner ads with simplified configuration
 */
fun Fragment.loadBannerAds(
    screen: String,
    topContainer: FrameLayout? = null,
    topShimmer: View? = null,
    bottomContainer: FrameLayout? = null,
    bottomShimmer: View? = null
) {
    val adsManager = cachedAdsManager ?: run {
        Timber.e("[$screen] AdsManager unavailable, skipping banner load")
        return
    }
    val context = context ?: return

    viewLifecycleOwner.lifecycleScope.launch {
        val configs = buildList {
            if (topContainer != null && topShimmer != null) {
                add(BannerConfig("top", topContainer, topShimmer))
            }
            if (bottomContainer != null && bottomShimmer != null) {
                add(BannerConfig("bottom", bottomContainer, bottomShimmer))
            }
        }

        if (configs.isNotEmpty()) {
            adsManager.loadBannerAds(context, screen, configs)
            delay(1500)
            val needsRetry = configs.any { it.container.childCount == 0 }
            if (needsRetry) {
                Timber.w("[$screen] Banner retry: config may not be ready on first pass")
                adsManager.loadBannerAds(context, screen, configs)
            }
        }
    }
}

/**
 * Load native ads with simplified configuration
 */
fun Fragment.loadNativeAds(
    screen: String,
    topContainer: FrameLayout? = null,
    topShimmer: FrameLayout? = null,
    centerContainer: FrameLayout? = null,
    centerShimmer: FrameLayout? = null,
    bottomContainer: FrameLayout? = null,
    bottomShimmer: FrameLayout? = null,
    onEvent: ((NativeAdEvent) -> Unit)? = null
) {
    val adsManager = cachedAdsManager ?: run {
        Timber.e("[$screen] AdsManager is null")
        return
    }

    val context = context ?: return

    viewLifecycleOwner.lifecycleScope.launch {
        val offPositions = mutableSetOf<String>()
        val eventCallback: (NativeAdEvent) -> Unit = { event ->
            when (event) {
                is NativeAdEvent.Off -> offPositions += event.position.lowercase()
                is NativeAdEvent.AllOffFromConfig -> {
                    offPositions += "top"
                    offPositions += "center"
                    offPositions += "bottom"
                }
                else -> Unit
            }
            onEvent?.invoke(event)
        }

        val configs = buildList {
            if (topContainer != null && topShimmer != null) {
                add(NativeAdConfig("top", topContainer, topShimmer))
            }
            if (centerContainer != null && centerShimmer != null) {
                add(NativeAdConfig("center", centerContainer, centerShimmer))
            }
            if (bottomContainer != null && bottomShimmer != null) {
                add(NativeAdConfig("bottom", bottomContainer, bottomShimmer))
            }
        }

        if (configs.isEmpty()) {
            Timber.w("[$screen] No native ad configs")
            return@launch
        }

        adsManager.loadNativeAds(context, screen, configs, eventCallback)
        delay(1500)
        // Only retry the configs whose containers are still empty — re-running the full
        // list re-prepares already-loaded containers (clears views, re-shows shimmer).
        val pendingConfigs = configs.filter {
            it.container.childCount == 0 && it.position.lowercase() !in offPositions
        }
        if (pendingConfigs.isNotEmpty()) {
            Timber.w("[$screen] Native retry for: ${pendingConfigs.map { it.position }}")
            adsManager.loadNativeAds(context, screen, pendingConfigs, eventCallback)
        }
    }
}

/**
 * Show interstitial with navigation
 */
fun Fragment.showInterstitialAndNavigate(
    screen: String,
    trigger: String,
    noCounterNeeded: Boolean = false,
    onAdResult: (wasAdShown: Boolean) -> Unit = {}
) {
    val effectiveNoCounter = noCounterNeeded || PRE_HOME_COUNTER_BYPASS_SCREENS.contains(screen)
    Log.d(TAG_INTER, "showInterstitialAndNavigate screen=$screen trigger=$trigger noCounterNeeded=$noCounterNeeded effectiveNoCounter=$effectiveNoCounter")
    val activity = activity ?: run {
        Log.d(TAG_INTER, "showInterstitialAndNavigate blocked: activity null")
        onAdResult(false)
        return
    }

    if (!isAdded || activity.isFinishing) {
        Log.d(TAG_INTER, "showInterstitialAndNavigate blocked: fragment not added or activity finishing")
        onAdResult(false)
        return
    }

    viewLifecycleOwner.lifecycleScope.launch {
        cachedAdsManager?.showInterstitialAd(
            activity,
            screen,
            trigger,
            effectiveNoCounter,
            onAdClosed = { onAdResult(true) },
            onAdNotShown = { onAdResult(false) }
        ) ?: onAdResult(false)
    }
}

/**
 * Show interstitial (must show)
 */
fun Fragment.showInterstitialMust(
    onAdResult: (wasAdShown: Boolean) -> Unit = {}
) {
    val activity = activity ?: run {
        onAdResult(false)
        return
    }

    if (!isAdded || view == null || activity.isFinishing) {
        Timber.w("Fragment not ready for interstitial")
        onAdResult(false)
        return
    }

    viewLifecycleOwner.lifecycleScope.launch {
        cachedAdsManager?.showInterstitialMust(
            activity,
            onAdNotShown = { onAdResult(false) },
            onAdClosed = { onAdResult(true) }
        ) ?: onAdResult(false)
    }
}

fun Fragment.safeShowInterstitialBackNav(
    screenName: String,
    direction: String,
    expectedDestinationId: Int
) {
    if (!isAdded) return

    showInterstitialAndNavigate(screenName, direction) {
        lifecycleScope.launch {
            lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {
                try {
                    val navController = findNavController()
                    val currentDestinationId = navController.currentDestination?.id

                    if (currentDestinationId == expectedDestinationId) {
                        navController.popBackStack()
                    } else {
                        Timber.w("❗Blocked Back Nav: Current destination is $currentDestinationId, expected $expectedDestinationId")
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "🚫 Navigation failed: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.e(e, "🚫 Illegal state during navigation: ${e.message}")
                }
            }
        }
    }
}

fun Fragment.safeShowInterstitialNavigate(
    screenName: String,
    direction: String,
    noCounterNeeded: Boolean = false,
    afterAd: (() -> Unit)? = null
) {
    Log.d(TAG_INTER, "safeShowInterstitialNavigate screen=$screenName trigger=$direction noCounterNeeded=$noCounterNeeded")
    // Abort if fragment is not attached
    if (!isAdded) {
        Log.d(TAG_INTER, "safeShowInterstitialNavigate blocked: fragment not added")
        afterAd?.invoke() // fallback
        return
    }

    // Use activity context for ad
    val activityContext = activity
    if (activityContext == null || activityContext.isFinishing) {
        Log.d(TAG_INTER, "safeShowInterstitialNavigate blocked: activity null/finishing")
        afterAd?.invoke()
        return
    }

    try {
        // Show the interstitial ad
        showInterstitialAndNavigate(screenName, direction, noCounterNeeded) { adShown ->
            Log.d(TAG_INTER, "safeShowInterstitialNavigate result adShown=$adShown screen=$screenName trigger=$direction")
            // Wait for the fragment to resume before navigating — the interstitial
            // dismissal callback fires while the fragment is still in the background,
            // so navigating immediately causes NavController to silently drop the call.
            lifecycleScope.launch {
                lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {
                    if (!isAdded) return@withStateAtLeast
                    try {
                        afterAd?.invoke()
                    } catch (e: Exception) {
                        Timber.e(e, "🚫 Error executing after-ad action: ${e.message}")
                    }
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "🚫 Failed to show interstitial: ${e.message}")
        afterAd?.invoke()
    }
}

/**
 * Check if ads should be loaded
 */
suspend fun Fragment.shouldLoadAds(): Boolean {
    if (!isAdded || view == null || activity == null) {
        Timber.w("Fragment not attached")
        return false
    }

    val context = requireContext()
    val adsManager = cachedAdsManager ?: return false

    return try {
        adsManager.shouldLoadAds(context)
    } catch (e: Exception) {
        Timber.w(e, "Error checking shouldLoadAds")
        false
    }
}

/**
 * Load standard screen ads (all types)
 */
fun Fragment.loadStandardScreenAds(
    screen: String,
    bannerTopLayout: ViewGroup? = null,
    bannerBottomLayout: ViewGroup? = null,
    nativeTopLayout: ViewGroup? = null,
    nativeCenterLayout: ViewGroup? = null,
    nativeBottomLayout: ViewGroup? = null,
    onEvent: ((NativeAdEvent) -> Unit)? = null
) {
    // Post all operations to the view queue to ensure layout is complete
    val fragmentView = view ?: return
    fragmentView.post {
        if (!isAdded) return@post

        // Extract banner views
        val bannerTopContainer = bannerTopLayout?.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = bannerTopLayout?.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        val bannerBottomContainer = bannerBottomLayout?.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerBottomShimmer = bannerBottomLayout?.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)

        // Load banners
        loadBannerAds(
            screen,
            bannerTopContainer,
            bannerTopShimmer,
            bannerBottomContainer,
            bannerBottomShimmer
        )

        // Extract native views
        val nativeTopContainer = nativeTopLayout?.findViewById<FrameLayout>(R.id.admob_native)
        val nativeTopShimmer = nativeTopLayout?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeCenterContainer = nativeCenterLayout?.findViewById<FrameLayout>(R.id.admob_native)
        val nativeCenterShimmer = nativeCenterLayout?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeBottomContainer = nativeBottomLayout?.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer = nativeBottomLayout?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)

        // Load natives
        loadNativeAds(
            screen,
            nativeTopContainer,
            nativeTopShimmer,
            nativeCenterContainer,
            nativeCenterShimmer,
            nativeBottomContainer,
            nativeBottomShimmer,
            onEvent
        )
    }
}
