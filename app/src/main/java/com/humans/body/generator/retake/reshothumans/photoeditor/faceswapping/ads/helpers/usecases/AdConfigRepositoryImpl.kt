package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.RemoteScreens
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.AdConfigInitializer
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import timber.log.Timber
import javax.inject.Inject

class AdConfigRepositoryImpl @Inject constructor(
    private val adConfigInitializer: AdConfigInitializer
) : AdConfigRepository {

    @Volatile
    private var configLoaded = false

    override fun initialize(onReady: () -> Unit, onFailed: () -> Unit) {
        adConfigInitializer.setListener(
            onReady = {
                configLoaded = true
                onReady()
            },
            onFailed = {
                configLoaded = false
                Timber.w("Ad configuration failed to load")
                onFailed()
            }
        )
    }

    override fun isConfigLoaded(): Boolean = configLoaded

    override fun getInterstitialAdUnitId(screen: String): String {
        return when (screen) {
            RemoteScreens.SPLASH_SCREEN -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_INTERSTITIAL_SPLASH_AD_ID else ADS.PROD_ADMOB_INTERSTITIAL_SPLASH_AD_ID
            RemoteScreens.INTRO_SCREEN -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_INTERSTITIAL_ONBOARDING_AD_ID else ADS.PROD_ADMOB_INTERSTITIAL_ONBOARDING_AD_ID
            RemoteScreens.LANGUAGE_SCREEN -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_INTERSTITIAL_LANGUAGE_AD_ID else ADS.PROD_ADMOB_INTERSTITIAL_LANGUAGE_AD_ID
            else -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_INTERSTITIAL_AD_ID else ADS.PROD_ADMOB_INTERSTITIAL_AD_ID
        }
    }

    override fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) ADS.TEST_ADMOB_BANNER_AD_ID else ADS.PROD_ADMOB_BANNER_AD_ID
    }

    override fun getNativeAdUnitId(screen: String): String {
        return when (screen) {
            RemoteScreens.INTRO_SCREEN -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_NATIVE_ONBOARDING_AD_ID else ADS.PROD_ADMOB_NATIVE_ONBOARDING_AD_ID
            RemoteScreens.SPLASH_SCREEN -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_NATIVE_SPLASH_AD_ID else ADS.PROD_ADMOB_NATIVE_SPLASH_AD_ID
            RemoteScreens.LANGUAGE_SCREEN -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_NATIVE_LANGUAGE_AD_ID else ADS.PROD_ADMOB_NATIVE_LANGUAGE_AD_ID
            else -> if (BuildConfig.DEBUG) ADS.TEST_ADMOB_NATIVE_AD_ID else ADS.PROD_ADMOB_NATIVE_AD_ID
        }
    }
}