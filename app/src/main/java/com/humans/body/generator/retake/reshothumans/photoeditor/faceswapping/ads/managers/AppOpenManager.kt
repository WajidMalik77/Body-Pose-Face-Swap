package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdShowCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdStateManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.DebugToaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class AppOpenManager private constructor(context: Context) {
    private val appContext: Context = context.applicationContext
    private val appOpenAdRef = AtomicReference<AppOpenAd?>()
    private val loadTimeRef = AtomicLong(0L)
    private val currentActivityRef = AtomicReference<WeakReference<Activity>?>()
    private val isDestroyed = AtomicBoolean(false)
    private val isAdLoading = AtomicBoolean(false)
    private val isAppOpenAdShowing = AtomicBoolean(false)
    private val bgDispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + bgDispatcher)
    private var lastAdUnitId: String? = null

    /*    private val prefs: SharedPreferences =
            context.getSharedPreferences("app_open_ad_prefs", Context.MODE_PRIVATE)

        private var adLoadCount: Int
            get() = prefs.getInt(KEY_AD_LOAD_COUNT, 0)
            set(value) = prefs.edit { putInt(KEY_AD_LOAD_COUNT, value) }*/

    companion object {
        @Volatile
        private var INSTANCE: AppOpenManager? = null
        private const val TAG_AO = "AppOpenTrace"
//        private const val KEY_AD_LOAD_COUNT = "app_open_ad_load_count"

        fun getInstance(context: Context): AppOpenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppOpenManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun updateCurrentActivity(activity: Activity?) {
        currentActivityRef.set(activity?.let { WeakReference(it) })
    }

    fun loadAppOpenAd(
        adUnitId: String,
        adsPref: AdsPref,
        onAdLoaded: () -> Unit? = {},
        onAdFailed: (LoadAdError) -> Unit? = {},
    ) {
        Log.d(TAG_AO, "load requested id=$adUnitId")
        if (isAdAvailable()) {
            Log.d(TAG_AO, "load skipped: ad already available")
            onAdLoaded()
            return
        }

        if (isAdLoading.getAndSet(true)) {
            Log.d(TAG_AO, "load skipped: already in progress")
            return
        }

        if (isDestroyed.get()) {
            Log.w(TAG_AO, "load blocked: manager destroyed")
            isAdLoading.set(false)
            return
        }

        lastAdUnitId = adUnitId

        // Build request off main thread
        scope.launch {
            val request = buildAdRequest(adsPref)

            withContext(Dispatchers.Main) {
                loadAdOnMainThread(
                    adUnitId,
                    request,
                    adsPref,
                    onAdLoaded = { onAdLoaded.invoke() },
                    onAdFailed = { error ->
                        onAdFailed.invoke(error)
                    }
                )
            }
        }
    }

    private fun buildAdRequest(adsPref: AdsPref): AdRequest {
        val requestBuilder = AdRequest.Builder()

        if (adsPref.isNpa()) {
            val extras = Bundle()
            extras.putString("npa", "1")
            requestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        return requestBuilder.build()
    }

    private fun loadAdOnMainThread(
        adUnitId: String,
        request: AdRequest,
        adsPref: AdsPref,
        onAdLoaded: () -> Unit,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        AppOpenAd.load(
            appContext,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    DebugToaster.showAdDebugCard(appContext, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Loaded")
                    handleAdLoaded(ad, onAdLoaded)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    DebugToaster.showAdDebugCard(appContext, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Failed to Load")
                    handleAdLoadFailed(error, onAdFailed)
                }
            }
        )
    }

    private fun handleAdLoaded(
        ad: AppOpenAd,
        onAdLoaded: () -> Unit
    ) {
        Timber.d("Ad Loaded: AppOpenAd")
        if (isDestroyed.get()) {
            ad.fullScreenContentCallback = null
            return
        }

        // Lock-free updates
        appOpenAdRef.set(ad)
        loadTimeRef.set(System.currentTimeMillis())
        isAdLoading.set(false)
//        adLoadCount++

        onAdLoaded()
    }

    private fun handleAdLoadFailed(
        error: LoadAdError,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        Timber.d("Ad Failed: ${error.message}")

        isAdLoading.set(false)
//        appOpenDialog?.safeDismiss()

        onAdFailed(error)
    }

    fun showAppOpenAdIfAvailable(adsPref: AdsPref, callback: AdShowCallback) {
        val skipReason = getSkipReason()
        if (skipReason != null) {
            Log.d(TAG_AO, "show blocked: $skipReason")
            callback.onAdFailedToShow(skipReason)
            return
        }

        Log.d(TAG_AO, "show scheduled")
        scheduleAdShow(adsPref, callback)
    }

    private fun scheduleAdShow(adsPref: AdsPref, callback: AdShowCallback) {
        val activity = currentActivityRef.get()?.get() ?: run {
            Log.d(TAG_AO, "show blocked: no active activity at schedule")
            return
        }

        Choreographer.getInstance().postFrameCallback {
            showAdOnNextFrame(activity, adsPref, callback)
        }
    }

    private fun showAdOnNextFrame(activity: Activity, adsPref: AdsPref, callback: AdShowCallback) {
        val ad = appOpenAdRef.get() ?: run {
            Log.d(TAG_AO, "show blocked: ad ref null on next frame")
            callback.onAdFailedToShow("Ad not available")
            return
        }

        if (!isAppOpenAdShowing.compareAndSet(false, true)) {
            Log.d(TAG_AO, "show blocked: already showing")
            callback.onAdFailedToShow("Ad already showing")
            return
        }

        // Set callback
        ad.fullScreenContentCallback = createFullScreenCallback(activity, adsPref, callback)

        try {
            // Pre-warm the ad container to reduce first-frame jank
            activity.window?.decorView?.post {
                Log.d(TAG_AO, "show executing ad.show()")
                ad.show(activity)
            }
        } catch (e: Exception) {
            handleShowException(e, callback)
        }
    }

    private fun getSkipReason(): String? {
        return when {
            isDestroyed.get() -> "AppOpenManager is destroyed"
            isAppOpenAdShowing.get() -> "An App Open Ad is already showing"
            currentActivityRef.get()?.get() == null -> "No active activity"
            !isAdAvailable() -> "No App Open Ad loaded"
            wasLoadTimeLessThanNHoursAgo().not() -> "Ad expired"
            else -> null
        }
    }

    private fun createFullScreenCallback(
        activity: Activity,
        adsPref: AdsPref,
        callback: AdShowCallback
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Shown")
                handleAdShown(callback)
            }

            override fun onAdDismissedFullScreenContent() {
                handleAdDismissed(callback)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Failed to Show")
                handleShowFailure(adError, callback)
            }

            override fun onAdImpression() {}
        }
    }

    private fun handleAdShown(callback: AdShowCallback) {
        Log.d(TAG_AO, "show success")
        AdStateManager.isAppOpenAdShowing = true
        appOpenAdRef.set(null)

        callback.onAdShown()
    }

    private fun handleAdDismissed(callback: AdShowCallback) {
        Log.d(TAG_AO, "show dismissed")
        isAppOpenAdShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false

        callback.onAdDismissed()
    }

    private fun handleShowFailure(adError: AdError, callback: AdShowCallback) {
        Log.w(TAG_AO, "show failed code=${adError.code} message=${adError.message}")
        appOpenAdRef.set(null)
        isAppOpenAdShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false

        callback.onAdFailedToShow("Ad failed: [${adError.code}] ${adError.message}")
    }

    private fun handleShowException(e: Exception, callback: AdShowCallback) {
        Log.e(TAG_AO, "show exception", e)
        isAppOpenAdShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false

        callback.onAdFailedToShow("Exception: ${e.localizedMessage ?: "Unknown"}")
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAdRef.get() != null && wasLoadTimeLessThanNHoursAgo()
    }

    fun isAdLoading(): Boolean {
        return isAdLoading.get()
    }

    fun hasUsableAd(): Boolean {
        return isAdAvailable()
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = System.currentTimeMillis() - loadTimeRef.get()
        val numMilliSecondsPerHour = 3600000L
        return dateDifference < numMilliSecondsPerHour * 4
    }

    fun destroy() {
        isDestroyed.set(true)
        scope.cancel()
        appOpenAdRef.getAndSet(null)?.fullScreenContentCallback = null
//        appOpenDialog?.safeDismiss()
//        appOpenDialog = null
        currentActivityRef.set(null)
    }
}
