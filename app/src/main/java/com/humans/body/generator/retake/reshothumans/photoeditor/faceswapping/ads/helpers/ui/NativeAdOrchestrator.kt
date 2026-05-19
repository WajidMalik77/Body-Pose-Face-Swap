package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui

import android.content.Context
import android.widget.FrameLayout
import com.google.android.gms.ads.LoadAdError
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.NativeAdEvent
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.AdConfigRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.NativeAdRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.AdmobNativeManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models.NativeAdColorConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.ActivityNative
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class NativeAdOrchestrator @Inject constructor(
    private val nativeAdRepository: NativeAdRepository,
    private val adConfigRepository: AdConfigRepository,
    @param:ActivityNative private val admobNativeManager: AdmobNativeManager,
    private val checkEligibility: CheckAdEligibilityUseCase,
    private val adsPref: AdsPref
) {

    suspend fun loadNativeAds(
        context: Context,
        screen: String,
        nativeConfigs: List<NativeAdConfig>,
        onEvent: ((NativeAdEvent) -> Unit)? = null
    ) {
        // Check eligibility
        val eligibility = checkEligibility(context)
        if (!eligibility.canShowAds) {
            Timber.w("[$screen] ${eligibility.reason} — skipping ads")
            onEvent?.invoke(NativeAdEvent.AllOffFromConfig)
            return
        }

        // Check if any ads are visible
        val allAdsInvisible = nativeConfigs.all {
            !nativeAdRepository.getNativeVisibility(screen, it.position)
        }

        if (allAdsInvisible) {
            Timber.w("[$screen] No visible native ads for this screen")
            onEvent?.invoke(NativeAdEvent.AllOffFromConfig)
            return
        }

        // AdmobNativeManager owns one active load at a time. Load multi-placement
        // screens sequentially so top/bottom placements do not silently skip each other.
        loadNativeConfigsSequentially(screen, nativeConfigs, 0, onEvent)
    }

    private fun loadNativeConfigsSequentially(
        screen: String,
        nativeConfigs: List<NativeAdConfig>,
        index: Int,
        onEvent: ((NativeAdEvent) -> Unit)?
    ) {
        if (index >= nativeConfigs.size) return
        loadSingleNativeAd(screen, nativeConfigs[index], onEvent) {
            loadNativeConfigsSequentially(screen, nativeConfigs, index + 1, onEvent)
        }
    }

    private fun loadSingleNativeAd(
        screen: String,
        config: NativeAdConfig,
        onEvent: ((NativeAdEvent) -> Unit)?,
        onFinished: () -> Unit
    ) {
        val position = config.position
        val visible = nativeAdRepository.getNativeVisibility(screen, position)

        if (!visible) {
            Timber.d("[$screen][$position] Native ad off from config")
            onEvent?.invoke(NativeAdEvent.Off(position))
            onFinished()
            return
        }

        val showAfter = nativeAdRepository.getNativeAdFrequency(screen, position)
        val nativeLimit = nativeAdRepository.getNativeAdLimit(screen, position)
        val canShowByRate = adsPref.shouldShowNativeAd(screen, position, showAfter, nativeLimit)

        if (!canShowByRate) {
            Timber.d(
                "[$screen][$position] Native ad skipped by show rate showAfter=$showAfter limit=$nativeLimit"
            )
            onEvent?.invoke(NativeAdEvent.Off(position))
            onFinished()
            return
        }

        val size = nativeAdRepository.getNativeAdSize(screen, position)
        val theme = nativeAdRepository.getNativeAdColorConfig(screen, position)
        val adUnitId = adConfigRepository.getNativeAdUnitId(screen, position)
        val shouldPreload = nativeAdRepository.shouldNativePreload(screen, position)

        Timber.d("Should Native Preload: $screen : $position : $shouldPreload")
        loadNativeAdBySize(
            size = size,
            adUnitId = adUnitId,
            theme = theme,
            container = config.container,
            shimmer = config.shimmer,
            shouldPreload = shouldPreload,
            onImpression = {
                adsPref.recordNativeAdShown(screen, position)
            },
            onLoaded = {
                onEvent?.invoke(NativeAdEvent.Loaded(position))
                onFinished()
            },
            onFailed = { error ->
                onEvent?.invoke(NativeAdEvent.Failed(position, error))
                onFinished()
            }
        )
    }

    private fun loadNativeAdBySize(
        size: Int,
        adUnitId: String,
        theme: NativeAdColorConfig?,
        container: FrameLayout,
        shimmer: FrameLayout,
        shouldPreload: Boolean = true,
        onImpression: () -> Unit,
        onLoaded: () -> Unit,
        onFailed: (LoadAdError) -> Unit
    ) {
        if (size == 1) {
            admobNativeManager.loadNativeSmallAd(
                container, adUnitId, shimmer, R.layout.native_ad_shimmer_small, theme, shouldPreload,
                onImpression = onImpression,
                onLoaded = {
                    Timber.i("Small native ad loaded")
                    onLoaded()
                },
                onFailed = { error ->
                    Timber.e("Small native ad failed: ${error.message}")
                    onFailed(error)
                }
            )
        } else {
            val (layoutRes, shimmerRes) = when (size) {
                // placement_values:
                // 0=off, 1=small, 2=medium, 3=medium_top_cta,
                // 4=medium_side_media, 5=medium_compact, 6=large_center
                2 -> R.layout.native_medium to R.layout.native_loading_medium
                3 -> R.layout.native_medium_top_cta to R.layout.native_loading_medium_top_cta
                4 -> R.layout.native_medium_side_media to R.layout.native_loading_medium_side_media
                5 -> R.layout.native_medium_compact to R.layout.native_loading_medium_compact
                6 -> R.layout.native_large_center to R.layout.native_loading_large_center
                else -> R.layout.native_medium to R.layout.native_loading_medium
            }

            admobNativeManager.loadNativeCustomAd(
                adContainer = container,
                adUnitId = adUnitId,
                layoutRes = layoutRes,
                shimmerContainer = shimmer,
                shimmerLayout = shimmerRes,
                shouldPreloadNext = shouldPreload,
                colorConfig = theme,
                onImpression = onImpression,
                onLoaded = {
                    Timber.i("Native ad loaded for size=$size (layout=$layoutRes)")
                    onLoaded()
                },
                onFailed = { error ->
                    Timber.e("Native ad failed for size=$size: ${error.message}")
                    onFailed(error)
                }
            )
        }
    }
}
