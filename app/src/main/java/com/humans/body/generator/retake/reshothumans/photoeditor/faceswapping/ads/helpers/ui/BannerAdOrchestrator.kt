package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.ActivityBanner
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.AdConfigRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.BannerAdRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.BannerAdsManager
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class BannerAdOrchestrator @Inject constructor(
    private val bannerAdRepository: BannerAdRepository,
    private val adConfigRepository: AdConfigRepository,
    @param:ActivityBanner private val bannerAdsManager: BannerAdsManager,
    private val checkEligibility: CheckAdEligibilityUseCase
) {

    suspend fun loadBannerAd(
        context: Context,
        screen: String,
        position: String,
        container: FrameLayout,
        shimmer: View,
        adId: String? = null
    ) {
        // Check eligibility
        val eligibility = checkEligibility(context)
        if (!eligibility.canShowAds) {
            shimmer.visibility = View.GONE
            container.visibility = View.GONE
            return
        }

        // Get configuration
        val visible = bannerAdRepository.getBannerVisibility(screen, position)
        val type = bannerAdRepository.getBannerType(screen, position)
        val bannerId = adId ?: adConfigRepository.getBannerAdUnitId()

        // Handle visibility
        shimmer.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) {
            container.visibility = View.GONE
            return
        }

        // Load appropriate banner type
        loadBannerByType(type, position, container, bannerId, shimmer)
    }

    private fun loadBannerByType(
        type: String,
        position: String,
        container: FrameLayout,
        bannerId: String,
        shimmer: View
    ) {
        val onLoaded = { shimmer.visibility = View.GONE }
        val onFailed = { shimmer.visibility = View.GONE }

        when (type) {
            "a" -> bannerAdsManager.loadAdaptiveBanner(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed }
            )

            "r" -> bannerAdsManager.loadRectangleAd(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed }
            )

            "c" -> loadCollapsibleBanner(position, container, bannerId, onLoaded, onFailed)
            else -> container.visibility = View.GONE
        }
    }

    private fun loadCollapsibleBanner(
        position: String,
        container: FrameLayout,
        bannerId: String,
        onLoaded: () -> Unit,
        onFailed: () -> Unit
    ) {
        when (position) {
            "top" -> bannerAdsManager.loadCollapsibleTopBanner(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed }
            )

            "bottom" -> bannerAdsManager.loadCollapsibleBottomBanner(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed }
            )

            else -> container.visibility = View.GONE
        }
    }

    fun destroyAllBanners() {
        bannerAdsManager.destroyAllBanners()
    }
}