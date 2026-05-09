package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RewardedAds(
    val enabled: Int = 1,
    @SerialName("rewarded_ads_before_premium")
    val rewardedAdsBeforePremium: Int = 2,
    val placements: Map<String, Int> = emptyMap()
)
