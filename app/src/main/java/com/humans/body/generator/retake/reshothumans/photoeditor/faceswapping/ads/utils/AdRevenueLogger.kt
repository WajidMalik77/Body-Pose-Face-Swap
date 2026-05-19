package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import timber.log.Timber

object AdRevenueLogger {
    private const val EVENT_AD_IMPRESSION = "ad_impression"

    fun logPaidAdImpression(
        adPlatform: String = "admob",
        adSource: String?,
        adUnitName: String?,
        adFormat: String,
        adValue: AdValue,
        responseInfo: ResponseInfo?
    ) {
        val valueMicros = adValue.valueMicros
        val valueUsd = valueMicros / 1_000_000.0

        Firebase.analytics.logEvent(EVENT_AD_IMPRESSION) {
            param(FirebaseAnalytics.Param.AD_PLATFORM, adPlatform)
            param(FirebaseAnalytics.Param.AD_SOURCE, adSource ?: "unknown")
            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnitName ?: "unknown")
            param(FirebaseAnalytics.Param.AD_FORMAT, adFormat)
            param(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
            param(FirebaseAnalytics.Param.VALUE, valueUsd)
            param("ad_value_micros", valueMicros)
            param("precision_type", adValue.precisionType.toString())
            param("response_id", responseInfo?.responseId ?: "")
            param("mediation_adapter", responseInfo?.mediationAdapterClassName ?: "")
        }

        Timber.d(
            "Paid event logged format=%s micros=%d currency=%s source=%s unit=%s",
            adFormat,
            valueMicros,
            adValue.currencyCode,
            adSource,
            adUnitName
        )
    }
}
