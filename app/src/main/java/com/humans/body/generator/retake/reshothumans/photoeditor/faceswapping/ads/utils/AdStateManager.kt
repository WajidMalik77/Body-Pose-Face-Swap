package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils

object AdStateManager {
    var isInterstitialAdShowing: Boolean = false
    var isAppOpenAdShowing = false
    var isRewardedAdShowing = false

    fun isAnyAdShowing() = isInterstitialAdShowing ||
            isAppOpenAdShowing ||
            isRewardedAdShowing
}
