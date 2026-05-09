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
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class NativeAdOrchestrator @Inject constructor(
    private val nativeAdRepository: NativeAdRepository,
    private val adConfigRepository: AdConfigRepository,
    @param:ActivityNative private val admobNativeManager: AdmobNativeManager,
    private val checkEligibility: CheckAdEligibilityUseCase
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

        // Load each native ad
        nativeConfigs.forEach { config ->
            loadSingleNativeAd(screen, config, onEvent)
        }
    }

    private fun loadSingleNativeAd(
        screen: String,
        config: NativeAdConfig,
        onEvent: ((NativeAdEvent) -> Unit)?
    ) {
        val position = config.position
        val visible = nativeAdRepository.getNativeVisibility(screen, position)

        if (!visible) {
            Timber.d("[$screen][$position] Native ad off from config")
            onEvent?.invoke(NativeAdEvent.Off(position))
            return
        }

        val size = nativeAdRepository.getNativeAdSize(screen, position)
        val theme = nativeAdRepository.getNativeAdColorConfig(screen, position)
        val adUnitId = adConfigRepository.getNativeAdUnitId(screen)
        val shouldPreload = nativeAdRepository.shouldNativePreload(screen, position)

        Timber.d("Should Native Preload: $screen : $position : $shouldPreload")
        loadNativeAdBySize(
            size = size,
            adUnitId = adUnitId,
            theme = theme,
            container = config.container,
            shimmer = config.shimmer,
            shouldPreload = shouldPreload,
            onLoaded = { onEvent?.invoke(NativeAdEvent.Loaded(position)) },
            onFailed = { error -> onEvent?.invoke(NativeAdEvent.Failed(position, error)) }
        )
    }

    private fun loadNativeAdBySize(
        size: Int,
        adUnitId: String,
        theme: NativeAdColorConfig?,
        container: FrameLayout,
        shimmer: FrameLayout,
        shouldPreload: Boolean = true,
        onLoaded: () -> Unit,
        onFailed: (LoadAdError) -> Unit
    ) {
        val layout = when (size) {
            1 -> R.layout.native_ad_shimmer_small
            2 -> R.layout.native_ad_shimmer_medium
            else -> R.layout.native_ad_shimmer_small
        }

        if (size == 1) {
            admobNativeManager.loadNativeSmallAd(
                container, adUnitId, shimmer, layout, theme,shouldPreload,
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
            admobNativeManager.loadNativeMediumAd(
                container, adUnitId, shimmer, layout, shouldPreload,theme,
                onLoaded = {
                    Timber.i("Medium native ad loaded")
                    onLoaded()
                },
                onFailed = { error ->
                    Timber.e("Medium native ad failed: ${error.message}")
                    onFailed(error)
                }
            )
        }
    }
}
