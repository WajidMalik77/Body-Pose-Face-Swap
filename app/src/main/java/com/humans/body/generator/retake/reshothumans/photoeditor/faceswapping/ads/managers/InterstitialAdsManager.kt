package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers

import android.app.Activity
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdStateManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.DebugToaster
import timber.log.Timber
import java.lang.ref.WeakReference

class InterstitialAdsManager private constructor(private val adsPref: AdsPref) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isDestroyed = false
    private var isInterstitialAdShowing = false

/*    private val prefs: SharedPreferences =
        context.getSharedPreferences("interstitial_ad_prefs", Context.MODE_PRIVATE)

    private var adLoadCount: Int
        get() = prefs.getInt(KEY_AD_LOAD_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_AD_LOAD_COUNT, value) }

    private var adShowCount: Int
        get() = prefs.getInt(KEY_AD_SHOW_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_AD_SHOW_COUNT, value) }*/

    companion object {
        @Volatile
        private var INSTANCE: InterstitialAdsManager? = null
/*        private const val KEY_AD_LOAD_COUNT = "ad_load_count"
        private const val KEY_AD_SHOW_COUNT = "ad_show_count"*/

        fun getInstance(/*context: Context,*/ adsPref: AdsPref): InterstitialAdsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: InterstitialAdsManager(adsPref).also {
                    INSTANCE = it
                }
            }
        }
    }

    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        if (adsPref.isNpa()) {
            val extras = Bundle()
            extras.putString("npa", "1")
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            Timber.d("InterstitialAdsManager: Requesting NON-PERSONALIZED ads")
        } else {
            Timber.d("InterstitialAdsManager: Requesting PERSONALIZED ads")
        }
        return builder.build()
    }

    fun loadInterstitialAd(
        activity: Activity, interstitialAdUnitId: String, onAdLoaded: () -> Unit? = {},
        onAdFailedToLoad: (message: String) -> Unit? = {}
    ) {
        synchronized(this) {
            if (isDestroyed) {
                onAdFailedToLoad("Ad load aborted: Manager destroyed.")
                return
            }

            if (isLoading) {
                onAdFailedToLoad("Ad load skipped: Already in progress.")
                return
            }

            if (interstitialAd != null) {
                onAdLoaded()
                return
            }

            isLoading = true
        }

        val activityRef = WeakReference(activity)
        val adRequest = buildAdRequest()
        InterstitialAd.load(
            activity,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    val act = activityRef.get()
                    if (act == null || isDestroyed) {
                        Timber.d("Ad loaded but manager destroyed. Discarding ad.")
                        ad.fullScreenContentCallback = null
                        return
                    }
                    interstitialAd = ad
                    isLoading = false
//                    adLoadCount++
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "Interstitial: Loaded")

                    onAdLoaded()
//                    Timber.i("✅ Interstitial ad loaded. Total loaded: $adLoadCount")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    AdStateManager.isInterstitialAdShowing = false

                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "Interstitial: Failed to Load")
                    onAdFailedToLoad(error.message)
                    Timber.e("Failed to load interstitial ad: ${error.message}")
                }
            }
        )
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdFailedToShow: () -> Unit,
        onAdDismissed: () -> Unit
    ) {

        if (isInterstitialAdShowing) {
            Timber.w("Ad is already showing. Skipping.")
            return
        }

        val ad = interstitialAd
        val activityRef = WeakReference(activity)
        if (ad == null || isDestroyed) {
            Timber.w("Interstitial ad is not ready or activity is destroyed.")
            onAdFailedToShow()
            return
        }

        isInterstitialAdShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
//                Timber.i("📢 Interstitial ad shown. Total shown: $adShowCount")
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "Interstitial: Shown")
                AdStateManager.isInterstitialAdShowing = true
            }

            override fun onAdDismissedFullScreenContent() {
                Timber.i("🔙 Interstitial ad dismissed by user.")
                cleanUpAdState()
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e("❌ Failed to show interstitial ad: ${adError.message}")
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "Interstitial: Failed to Show")

                cleanUpAdState()
                onAdFailedToShow()
            }
        }

        Timber.d("Showing interstitial ad...")
        activityRef.get()?.let { ad.show(it) } ?: run {
            isInterstitialAdShowing = false
        }
    }

    /** Clears ad state flags after dismissal or failure */
    private fun cleanUpAdState() {
        interstitialAd = null
        isInterstitialAdShowing = false
        AdStateManager.isInterstitialAdShowing = false
//        adShowCount++
    }

}
