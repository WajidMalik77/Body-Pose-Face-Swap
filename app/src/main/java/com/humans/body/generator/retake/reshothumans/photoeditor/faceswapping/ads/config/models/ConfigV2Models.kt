package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val ads: Int = 1,
    @SerialName("app_open")
    val appOpen: AppOpenV2Config = AppOpenV2Config(),
    val screens: ScreensV2 = ScreensV2()
)

@Serializable
data class AppOpenV2Config(
    val splash: Int = 1,
    val resume: Int = 1,
    @SerialName("resume_min_background_seconds")
    val resumeMinBackgroundSeconds: Int = 1,
    @SerialName("show_after")
    val showAfter: Int? = null,
    @SerialName("splash_show_after")
    val splashShowAfter: Int? = null,
    @SerialName("resume_show_after")
    val resumeShowAfter: Int? = null,
    @SerialName("app_open_limit")
    val appOpenLimit: Int? = null,
    @SerialName("splash_limit")
    val splashLimit: Int? = null,
    @SerialName("resume_limit")
    val resumeLimit: Int? = null
)

@Serializable
data class ScreensV2(
    val first: LaunchScreens = LaunchScreens(),
    val second: LaunchScreens = LaunchScreens()
)

@Serializable
data class BannerV2Config(
    val enabled: Int = 1,
    val placements: Map<String, Int> = emptyMap()
)

@Serializable
data class InterstitialV2Config(
    val enabled: Int = 1,
    @SerialName("click_interval")
    val clickInterval: Int = 1,
    @SerialName("isInterFirstCount")
    val isInterFirstCount: Int = 1,
    @SerialName("cooldown_seconds")
    val cooldownSeconds: Int = 0,
    val placements: Map<String, Int> = emptyMap()
)

@Serializable
data class RewardedV2Config(
    val enabled: Int = 1,
    @SerialName("rewarded_ads_before_premium")
    val rewardedAdsBeforePremium: Int = 2,
    val placements: Map<String, Int> = emptyMap()
)

@Serializable
data class RemoteAdIdsConfig(
    val appOpen: Map<String, String> = emptyMap(),
    val interstitial: Map<String, String> = emptyMap(),
    val banner: Map<String, String> = emptyMap(),
    @SerialName("native")
    val nativeIds: Map<String, String> = emptyMap(),
    val rewarded: Map<String, String> = emptyMap()
)
