package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.NativeAdConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models.NativeAdColorConfig
import javax.inject.Inject

class NativeAdRepositoryImpl @Inject constructor(
    private val nativeAdConfigManager: NativeAdConfigManager
) : NativeAdRepository {

    override fun getNativeVisibility(screen: String, position: String): Boolean {
        return nativeAdConfigManager.isNativeVisible(screen, position)
    }

    override fun shouldNativePreload(
        screen: String,
        position: String
    ): Boolean {
        return nativeAdConfigManager.shouldNativePreload(screen, position)
    }

    override fun getNativeAdSize(screen: String, position: String): Int {
        return nativeAdConfigManager.getNativeAdSize(screen, position)
    }

    override fun getNativeAdColorConfig(
        screen: String,
        position: String
    ): NativeAdColorConfig? {
        return nativeAdConfigManager.getNativeAdColorConfig(screen, position)
    }
}