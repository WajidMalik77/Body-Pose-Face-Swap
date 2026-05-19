package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdRevenueLogger
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdStateManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdUnitIdSanitizer
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.DebugToaster

class RewardedAdsManager private constructor(private val adsPref: AdsPref) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isRewardedAdShowing = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingOnLoaded = mutableListOf<() -> Unit>()
    private val pendingOnFailed = mutableListOf<(String) -> Unit>()

    companion object {
        @Volatile
        private var INSTANCE: RewardedAdsManager? = null

        fun getInstance(adsPref: AdsPref): RewardedAdsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RewardedAdsManager(adsPref).also { INSTANCE = it }
            }
        }

        private const val REWARD_DISMISS_GRACE_MS = 2500L
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
        synchronized(this) {
            if (rewardedAd != null) {
                onLoaded?.invoke()
                return
            }

            onLoaded?.let { pendingOnLoaded += it }
            onFailed?.let { pendingOnFailed += it }

            if (isLoading) return
            isLoading = true
        }

        val requestedAdId = configuredAdUnitId?.takeIf { it.isNotBlank() } ?: resolveRewardedAdId(context)
        val adId = AdUnitIdSanitizer.sanitizeRewarded(requestedAdId)
        if (adId.isBlank()) {
            completePendingWithFailure("Rewarded ad id is blank")
            return
        }

        RewardedAd.load(
            context,
            adId,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    synchronized(this@RewardedAdsManager) {
                        rewardedAd = ad
                        ad.setOnPaidEventListener(OnPaidEventListener { adValue ->
                            AdRevenueLogger.logPaidAdImpression(
                                adSource = ad.responseInfo?.loadedAdapterResponseInfo?.adSourceName,
                                adUnitName = adId,
                                adFormat = "rewarded",
                                adValue = adValue,
                                responseInfo = ad.responseInfo
                            )
                        })
                        isLoading = false
                    }
                    completePendingWithSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    synchronized(this@RewardedAdsManager) {
                        rewardedAd = null
                        isLoading = false
                    }
                    completePendingWithFailure(error.message)
                }
            }
        )
    }

    fun show(
        activity: Activity,
        onAdShowing: (() -> Unit)? = null,
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
        var dismissed = false
        var resolved = false

        fun resolveSuccessOnce() {
            if (resolved) return
            resolved = true
            onRewardEarned()
        }

        fun resolveFailureOnce() {
            if (resolved) return
            resolved = true
            onFailedOrDismissedWithoutReward()
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isRewardedAdShowing = true
                AdStateManager.isRewardedAdShowing = true
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "Rewarded: Shown")
                onAdShowing?.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                cleanupAfterShow()
                dismissed = true
                if (rewardEarned) {
                    resolveSuccessOnce()
                } else {
                    // Business rule: once ad was shown and user closed it, continue the flow.
                    // Keep a short grace for late reward callbacks, then still resolve success.
                    mainHandler.postDelayed({
                        resolveSuccessOnce()
                    }, REWARD_DISMISS_GRACE_MS)
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                cleanupAfterShow()
                resolveFailureOnce()
            }
        }

        ad.show(activity) { _: RewardItem ->
            rewardEarned = true
            if (dismissed) {
                resolveSuccessOnce()
            }
        }
    }

    fun showOnDemand(
        activity: Activity,
        configuredAdUnitId: String? = null,
        onAdShowing: (() -> Unit)? = null,
        onRewardEarned: () -> Unit,
        onFailedOrDismissedWithoutReward: () -> Unit
    ) {
        val readyAd = rewardedAd
        if (readyAd != null) {
            show(activity, onAdShowing, onRewardEarned, onFailedOrDismissedWithoutReward)
            return
        }

        preload(
            context = activity.applicationContext,
            configuredAdUnitId = configuredAdUnitId,
            onLoaded = {
                show(activity, onAdShowing, onRewardEarned, onFailedOrDismissedWithoutReward)
            },
            onFailed = {
                onFailedOrDismissedWithoutReward()
            }
        )
    }

    private fun cleanupAfterShow() {
        rewardedAd = null
        isRewardedAdShowing = false
        AdStateManager.isRewardedAdShowing = false
    }

    private fun completePendingWithSuccess() {
        val successCallbacks: List<() -> Unit>
        synchronized(this) {
            successCallbacks = pendingOnLoaded.toList()
            pendingOnLoaded.clear()
            pendingOnFailed.clear()
        }
        successCallbacks.forEach { it.invoke() }
    }

    private fun completePendingWithFailure(message: String) {
        val failureCallbacks: List<(String) -> Unit>
        synchronized(this) {
            isLoading = false
            failureCallbacks = pendingOnFailed.toList()
            pendingOnLoaded.clear()
            pendingOnFailed.clear()
        }
        failureCallbacks.forEach { it.invoke(message) }
    }
}
