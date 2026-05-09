package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdsConfig(
    @SerialName("progress_bars")
    val progressBars: ProgressBars = ProgressBars(),
    @SerialName("app_open")
    val appOpen: AppOpenConfig = AppOpenConfig(),
    val interstitial: InterstitialAds = InterstitialAds(),
    val rewarded: RewardedAds = RewardedAds(),
    val banner: BannerAds = BannerAds(),
    @SerialName("native")
    val native: NativeAdSettingsConfig = NativeAdSettingsConfig(),
    val legal: LegalConfig = LegalConfig()
)
