package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

interface AdConfigRepository {
    fun initialize(onReady: () -> Unit, onFailed: () -> Unit)
    fun isConfigLoaded(): Boolean
    fun getInterstitialAdUnitId(screen: String): String
    fun getBannerAdUnitId(): String
    fun getNativeAdUnitId(screen: String): String
 }