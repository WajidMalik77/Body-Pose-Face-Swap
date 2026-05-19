package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig

object AdUnitIdSanitizer {
    private const val GOOGLE_TEST_PUBLISHER_PREFIX = "ca-app-pub-3940256099942544"

    fun sanitizeBanner(adUnitId: String): String {
        return sanitize(
            adUnitId = adUnitId,
            prodFallback = ADS.PROD_ADMOB_BANNER_AD_ID,
            debugFallback = ADS.TEST_ADMOB_BANNER_AD_ID
        )
    }

    fun sanitizeNative(adUnitId: String): String {
        return sanitize(
            adUnitId = adUnitId,
            prodFallback = ADS.PROD_ADMOB_NATIVE_AD_ID,
            debugFallback = ADS.TEST_ADMOB_NATIVE_AD_ID
        )
    }

    fun sanitizeInterstitial(adUnitId: String): String {
        return sanitize(
            adUnitId = adUnitId,
            prodFallback = ADS.PROD_ADMOB_INTERSTITIAL_AD_ID,
            debugFallback = ADS.TEST_ADMOB_INTERSTITIAL_AD_ID
        )
    }

    fun sanitizeRewarded(adUnitId: String): String {
        return sanitize(
            adUnitId = adUnitId,
            prodFallback = ADS.PROD_ADMOB_REWARDED_AD_ID,
            debugFallback = ADS.TEST_ADMOB_REWARDED_AD_ID
        )
    }

    fun sanitizeAppOpen(adUnitId: String): String {
        return sanitize(
            adUnitId = adUnitId,
            prodFallback = ADS.PROD_ADMOB_APP_OPEN_ID,
            debugFallback = ADS.TEST_ADMOB_APP_OPEN_ID
        )
    }

    private fun sanitize(adUnitId: String, prodFallback: String, debugFallback: String): String {
        val candidate = adUnitId.trim()

        if (BuildConfig.DEBUG) {
            if (candidate.startsWith(GOOGLE_TEST_PUBLISHER_PREFIX) && candidate.contains('/')) {
                return candidate
            }
            return debugFallback
        }

        if (candidate.isBlank()) return prodFallback
        return if (candidate.startsWith(GOOGLE_TEST_PUBLISHER_PREFIX)) {
            prodFallback
        } else {
            candidate
        }
    }
}
