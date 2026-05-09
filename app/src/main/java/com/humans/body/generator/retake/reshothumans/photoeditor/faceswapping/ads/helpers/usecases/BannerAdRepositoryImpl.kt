package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import javax.inject.Inject

class BannerAdRepositoryImpl @Inject constructor(
    private val adControlConfigManager: AdControlConfigManager
) : BannerAdRepository {

    override fun getBannerVisibility(screen: String, position: String): Boolean {
        return adControlConfigManager.isBannerVisible(screen, position)
    }

    override fun getBannerType(screen: String, position: String): String {
        return adControlConfigManager.getBannerType(screen, position)
    }
}