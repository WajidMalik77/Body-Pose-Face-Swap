package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdControlConfig(
    @SerialName("text_generation_api")
    val textGenerationApi: String = "",
    val app: AppConfig? = null,
    val banner: BannerV2Config? = null,
    val interstitial: InterstitialV2Config? = null,
    val rewarded: RewardedV2Config? = null,
    @SerialName("native")
    val nativeV2: NativeAdSettingsConfig? = null,
    @SerialName("show_screens")
    val showScreens: ShowScreens = ShowScreens(),
    val ads: AdsConfig = AdsConfig(),
    @SerialName("showPremiumHome")
    val showPremiumHome: Int = 1,
    @SerialName("showPremiumSettings")
    val showPremiumSettings: Int = 1
)
