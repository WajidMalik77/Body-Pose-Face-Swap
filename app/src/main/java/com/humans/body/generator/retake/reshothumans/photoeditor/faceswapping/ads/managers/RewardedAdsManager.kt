package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdStateManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.DebugToaster
import java.lang.ref.WeakReference

class RewardedAdsManager private constructor(private val adsPref: AdsPref) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isRewardedAdShowing = false

    companion object {
        @Volatile
        private var INSTANCE: RewardedAdsManager? = null

        fun getInstance(adsPref: AdsPref): RewardedAdsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RewardedAdsManager(adsPref).also { INSTANCE = it }
            }
        }
    }

    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        if (adsPref.isNpa()) {
            val extras = Bundle()
            extras.putString("npa", "1")
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        return builder.build()
    }

    private fun resolveRewardedAdId(context: Context): String {
        return if (BuildConfig.DEBUG) {
            ADS.TEST_ADMOB_REWARDED_AD_ID
        } else {
            ADS.PROD_ADMOB_REWARDED_AD_ID
        }
    }

    fun preload(context: Context, onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        preload(context, null, onLoaded, onFailed)
    }

    fun preload(
        context: Context,
        configuredAdUnitId: String? = null,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (isLoading || rewardedAd != null) {
            onLoaded?.invoke()
            return
        }

        val adId = configuredAdUnitId?.takeIf { it.isNotBlank() } ?: resolveRewardedAdId(context)
        if (adId.isBlank()) {
            onFailed?.invoke("Rewarded ad id is blank")
            return
        }

        isLoading = true
        RewardedAd.load(
            context,
            adId,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    onFailed?.invoke(error.message)
                }
            }
        )
    }

    fun show(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onFailedOrDismissedWithoutReward: () -> Unit
    ) {
        if (isRewardedAdShowing) return

        val ad = rewardedAd
        if (ad == null) {
            onFailedOrDismissedWithoutReward()
            return
        }

        var rewardEarned = false
        val activityRef = WeakReference(activity)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isRewardedAdShowing = true
                AdStateManager.isRewardedAdShowing = true
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "Rewarded: Shown")
            }

            override fun onAdDismissedFullScreenContent() {
                cleanupAfterShow()
                if (rewardEarned) {
                    onRewardEarned()
                } else {
                    onFailedOrDismissedWithoutReward()
                }
                activityRef.get()?.applicationContext?.let { preload(it) }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                cleanupAfterShow()
                onFailedOrDismissedWithoutReward()
                activityRef.get()?.applicationContext?.let { preload(it) }
            }
        }

        ad.show(activity) { _: RewardItem ->
            rewardEarned = true
        }
    }

    private fun cleanupAfterShow() {
        rewardedAd = null
        isRewardedAdShowing = false
        AdStateManager.isRewardedAdShowing = false
    }
}
