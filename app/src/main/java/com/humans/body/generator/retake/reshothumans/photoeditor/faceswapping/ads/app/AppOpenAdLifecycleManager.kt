package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import com.google.android.ump.UserMessagingPlatform
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.dialogs.LoadingDialog
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.AppOpenManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS.TEST_ADMOB_APP_OPEN_ID
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.ADS.TEST_ADMOB_APP_OPEN_SPLASH_ID
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdShowCallback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdStateManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdLifecycleManager @Inject constructor(
    private val adsPref: AdsPref,
    private val premiumRepository: PremiumRepository,
    private val adControlConfigManager: AdControlConfigManager,
    @param:ApplicationContext private val appContext: Context
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    companion object {
        private const val TAG_AO = "AppOpenTrace"
        private const val RESUME_LOAD_WAIT_MS = 3000L
        private const val RESUME_LOAD_POLL_MS = 250L
    }

    private var currentActivity: Activity? = null
    private lateinit var appOpenManager: AppOpenManager
    private var backgroundedAtMillis: Long = 0L
    private val resumeFlowInProgress = AtomicBoolean(false)
    private var resumeLoadingDialog: LoadingDialog? = null
    private val resumeDialogHandler = Handler(Looper.getMainLooper())

    fun initialize() {
        initializeAppOpenManager()
    }

    fun preloadSplashAd() {
        try {
            val canRequestAds = UserMessagingPlatform
                .getConsentInformation(appContext)
                .canRequestAds()
            if (canRequestAds && !premiumRepository.isPremiumUser() && adControlConfigManager.shouldShowAppOpenSplash()) {
                val adId = if (BuildConfig.DEBUG) {
                    TEST_ADMOB_APP_OPEN_SPLASH_ID
                } else {
                    adControlConfigManager.getProdAppOpenSplashAdUnitId(ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID)
                }
                if (adId.isNotBlank()) {
                    initializeAppOpenManager()
                    appOpenManager.loadAppOpenAd(adId, adsPref)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error preloading splash app open ad")
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        try {
            Log.d(TAG_AO, "onStart entered")
            if (!shouldShowAppOpenAd()) {
                Log.d(TAG_AO, "blocked: shouldShowAppOpenAd=false")
                return
            }

            val activity = currentActivity ?: run {
                Log.d(TAG_AO, "blocked: currentActivity=null")
                return
            }
            if (activity.localClassName == "com.google.android.gms.ads.AdActivity") {
                Log.d(TAG_AO, "blocked: current activity is AdActivity")
                return
            }

            if (isOnSplashScreen(activity)) {
                Timber.d("Skipping App Open ad - on SplashScreen")
                return
            }

            val minBackgroundSeconds = adControlConfigManager.getResumeMinBackgroundSeconds()
            if (minBackgroundSeconds > 0 && backgroundedAtMillis > 0L) {
                val elapsedSeconds = (System.currentTimeMillis() - backgroundedAtMillis) / 1000
                if (elapsedSeconds < minBackgroundSeconds) {
                    Timber.d("Skipping resume app open ad; elapsed=$elapsedSeconds sec, min=$minBackgroundSeconds sec")
                    return
                }
            }

            if (!shouldShowResumeAppOpenByRate()) {
                Log.d(TAG_AO, "blocked: resume app-open skipped by show rate")
                return
            }

            Log.d(TAG_AO, "passed lifecycle gates, loading/showing resume app-open")
            ensureResumeAppOpenLoadedAndShow()
        } catch (e: Exception) {
            Timber.e(e, "Error in onStart lifecycle event")
            // Silently handle to prevent crash
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMillis = System.currentTimeMillis()
        Log.d(TAG_AO, "onStop backgroundedAtMillis=$backgroundedAtMillis")
        dismissResumeLoading()
        super.onStop(owner)
    }

    private fun shouldShowAppOpenAd(): Boolean {
        return try {
            if (currentActivity?.javaClass?.simpleName == "Splash") {
                Log.d(TAG_AO, "shouldShow check blocked: Splash activity is foreground")
                return false
            }
            val canRequestAds = UserMessagingPlatform
                .getConsentInformation(appContext)
                .canRequestAds()
            val isPremium = premiumRepository.isPremiumUser()
            val managerReady = ::appOpenManager.isInitialized
            val anyAdShowing = AdStateManager.isAnyAdShowing()
            val resumeEnabled = adControlConfigManager.shouldShowAppOpenResume()
            Log.d(TAG_AO,
                "shouldShow check: canRequestAds=$canRequestAds premium=$isPremium managerReady=$managerReady anyAdShowing=$anyAdShowing resumeEnabled=$resumeEnabled"
            )
            canRequestAds && !isPremium && managerReady && !anyAdShowing && resumeEnabled
        } catch (e: Exception) {
            Timber.e(e, "Error checking if should show app open ad")
            false
        }
    }

    private fun isOnSplashScreen(activity: Activity): Boolean {
        return try {
            // The dedicated Splash activity has no NavHostFragment, so the fragment-based
            // detection below would return false and let the resume app-open flow fire
            // (with its own LoadingDialog) on top of the splash's own ad pipeline.
            if (activity::class.simpleName == "Splash") return true

            // Ensure activity is a FragmentActivity
            val fragmentActivity = activity as? FragmentActivity ?: return false

            // Check if activity is finishing or destroyed
            if (fragmentActivity.isFinishing || fragmentActivity.isDestroyed) {
                return false
            }

            // Get support fragment manager safely
            val supportFragmentManager = try {
                fragmentActivity.supportFragmentManager
            } catch (e: Throwable) {
                return false
            }

            // Check if fragment manager state is saved (operations not allowed)
            if (supportFragmentManager.isStateSaved || supportFragmentManager.isDestroyed) {
                return false
            }

            // Get primary navigation fragment safely
            val navHost = try {
                supportFragmentManager.primaryNavigationFragment as? NavHostFragment
            } catch (e: Throwable) {
                return false
            } ?: return false

            // Comprehensive fragment state validation
            if (!isFragmentSafeToAccess(navHost)) {
                return false
            }

            // Try to get child fragment manager with maximum safety
            val childFragmentManager = try {
                // Double-check fragment is still attached right before access
                if (!navHost.isAdded || navHost.isDetached) {
                    return false
                }
                navHost.childFragmentManager
            } catch (e: IllegalStateException) {
                // This is the exact exception from your crash
                Timber.w(e, "NavHostFragment not attached yet")
                return false
            } catch (e: Throwable) {
                // Catch any other unexpected errors
                Timber.e(e, "Unexpected error accessing childFragmentManager")
                return false
            }

            // Check if child fragment manager is in valid state
            if (childFragmentManager.isDestroyed || childFragmentManager.isStateSaved) {
                return false
            }

            // Get fragments list safely
            val fragmentsList = try {
                childFragmentManager.fragments
            } catch (e: Throwable) {
                Timber.w(e, "Error accessing fragments list")
                return false
            }

            // Get the currently visible fragment safely
            val currentFragment = try {
                fragmentsList
                    .asSequence()
                    .filterNotNull()
                    .firstOrNull { fragment ->
                        try {
                            fragment.isAdded &&
                                    fragment.isVisible &&
                                    !fragment.isDetached &&
                                    !fragment.isRemoving &&
                                    fragment.lifecycle.currentState != Lifecycle.State.DESTROYED
                        } catch (e: Throwable) {
                            // If checking fragment state fails, skip this fragment
                            false
                        }
                    }
            } catch (e: Throwable) {
                Timber.w(e, "Error filtering fragments")
                return false
            } ?: return false

            // Get fragment class name safely
            val isSplashScreen = try {
                currentFragment::class.simpleName == "SplashFragment"
            } catch (e: Throwable) {
                false
            }

            isSplashScreen

        } catch (e: IllegalStateException) {
            // Specific handling for IllegalStateException
            Timber.e(e, "IllegalStateException checking splash screen")
            false
        } catch (e: Throwable) {
            // Catch ALL errors including OutOfMemoryError, StackOverflowError, etc.
            Timber.e(e, "Critical error checking if on splash screen")
            false
        }
    }

    private fun isFragmentSafeToAccess(fragment: Fragment): Boolean {
        return try {
            // Fragment must be added
            if (!fragment.isAdded) return false

            // Fragment must not be detached
            if (fragment.isDetached) return false

            // Fragment must not be removing
            if (fragment.isRemoving) return false

            // Check lifecycle state
            val lifecycleState = try {
                fragment.lifecycle.currentState
            } catch (e: Throwable) {
                return false
            }

            // Must be at least CREATED (INITIALIZED is too early)
            // DESTROYED is obviously not valid
            when (lifecycleState) {
                Lifecycle.State.DESTROYED -> false
                Lifecycle.State.INITIALIZED -> false
                else -> true // CREATED, STARTED, RESUMED are all valid
            }

        } catch (e: Throwable) {
            // Any error in checking means not safe
            false
        }
    }

    private fun shouldShowResumeAppOpenByRate(): Boolean {
        val showAfter = adControlConfigManager.getAppOpenShowAfter("resume")
        val limit = adControlConfigManager.getAppOpenLimit("resume")
        return adsPref.shouldShowAppOpenAd("resume", showAfter, limit)
    }

    private fun showAppOpenAdDirect(onFinished: (() -> Unit)? = null) {
        try {
            appOpenManager.showAppOpenAdIfAvailable(adsPref, object : AdShowCallback {
                override fun onAdShown() {
                    adsPref.recordAppOpenAdShown("resume")
                    Timber.d("App Open ad shown")
                    onFinished?.invoke()
                }

                override fun onAdFailedToShow(adError: String) {
                    if (adError.contains("already showing", ignoreCase = true)) {
                        Timber.d("App Open show skipped: $adError")
                    } else {
                        Timber.w("App Open ad failed: $adError")
                    }
                    onFinished?.invoke()
                }

                override fun onAdDismissed() {
                    onFinished?.invoke()
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Error showing app open ad direct")
            onFinished?.invoke()
        }
    }

    private fun initializeAppOpenManager() {
        try {
            if (!premiumRepository.isPremiumUser()) {
                appOpenManager = AppOpenManager.getInstance(appContext as Application)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing app open manager")
        }
    }

    // ActivityLifecycleCallbacks
    override fun onActivityResumed(activity: Activity) {
        try {
            currentActivity = activity
            Log.d(TAG_AO, "onActivityResumed ${activity::class.java.simpleName}")
            if (::appOpenManager.isInitialized) {
                appOpenManager.updateCurrentActivity(activity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onActivityResumed")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        try {
            // ProcessLifecycleOwner#onStart can run before Activity#onResume.
            // Keep an early foreground activity reference so resume app-open logic can run.
            currentActivity = activity
            Log.d(TAG_AO, "onActivityStarted ${activity::class.java.simpleName}")
            if (::appOpenManager.isInitialized) {
                appOpenManager.updateCurrentActivity(activity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onActivityStarted")
        }
    }

    private fun ensureResumeAppOpenLoadedAndShow() {
        if (!resumeFlowInProgress.compareAndSet(false, true)) {
            Log.d(TAG_AO, "blocked: resume show flow already in progress")
            return
        }

        val resumeAdUnitId = if (BuildConfig.DEBUG) {
            TEST_ADMOB_APP_OPEN_ID
        } else {
            adControlConfigManager.getProdAppOpenResumeAdUnitId(ADS.PROD_ADMOB_APP_OPEN_ID)
        }
        if (resumeAdUnitId.isBlank()) {
            Log.w(TAG_AO, "blocked: resume ad unit id is blank")
            resumeFlowInProgress.set(false)
            return
        }

        if (appOpenManager.hasUsableAd(resumeAdUnitId)) {
            Log.d(TAG_AO, "resume path: using cached app-open ad, no loading dialog needed")
            showAppOpenAdDirect {
                dismissResumeLoading()
                resumeFlowInProgress.set(false)
            }
            return
        }

        if (appOpenManager.isAdLoading(resumeAdUnitId)) {
            Log.d(TAG_AO, "resume load already in progress for same unit, polling for readiness")
            showResumeLoadingIfPossible()
            waitForResumeAdAndShow(resumeAdUnitId)
            return
        }

        if (appOpenManager.isAdLoading()) {
            Log.d(TAG_AO, "resume load waiting for another app-open load to finish")
            showResumeLoadingIfPossible()
            waitForResumeLoadSlotAndLoad(resumeAdUnitId)
            return
        }

        Log.d(TAG_AO, "loading resume app-open ad id=$resumeAdUnitId")
        showResumeLoadingIfPossible()
        loadResumeAppOpenAndShow(resumeAdUnitId)
    }

    private fun loadResumeAppOpenAndShow(resumeAdUnitId: String) {
        appOpenManager.loadAppOpenAd(
            adUnitId = resumeAdUnitId,
            adsPref = adsPref,
            onAdLoaded = {
                Log.d(TAG_AO, "resume load success, calling show")
                showAppOpenAdDirect {
                    dismissResumeLoading()
                    resumeFlowInProgress.set(false)
                }
            },
            onAdFailed = {
                Log.w(TAG_AO, "resume load failed code=${it.code} message=${it.message}")
                dismissResumeLoading()
                resumeFlowInProgress.set(false)
                Unit
            }
        )
    }

    private fun waitForResumeAdAndShow(resumeAdUnitId: String) {
        val checks = (RESUME_LOAD_WAIT_MS / RESUME_LOAD_POLL_MS).toInt()
        var completed = 0
        fun poll() {
            if (appOpenManager.hasUsableAd(resumeAdUnitId)) {
                showAppOpenAdDirect {
                    dismissResumeLoading()
                    resumeFlowInProgress.set(false)
                }
                return
            }
            completed += 1
            if (completed >= checks) {
                dismissResumeLoading()
                resumeFlowInProgress.set(false)
                return
            }
            resumeDialogHandler.postDelayed({ poll() }, RESUME_LOAD_POLL_MS)
        }
        resumeDialogHandler.postDelayed({ poll() }, RESUME_LOAD_POLL_MS)
    }

    private fun waitForResumeLoadSlotAndLoad(resumeAdUnitId: String) {
        val checks = (RESUME_LOAD_WAIT_MS / RESUME_LOAD_POLL_MS).toInt()
        var completed = 0
        fun poll() {
            if (appOpenManager.hasUsableAd(resumeAdUnitId)) {
                showAppOpenAdDirect {
                    dismissResumeLoading()
                    resumeFlowInProgress.set(false)
                }
                return
            }
            if (!appOpenManager.isAdLoading()) {
                loadResumeAppOpenAndShow(resumeAdUnitId)
                return
            }
            completed += 1
            if (completed >= checks) {
                Log.w(TAG_AO, "resume load timed out waiting for app-open load slot")
                dismissResumeLoading()
                resumeFlowInProgress.set(false)
                return
            }
            resumeDialogHandler.postDelayed({ poll() }, RESUME_LOAD_POLL_MS)
        }
        resumeDialogHandler.postDelayed({ poll() }, RESUME_LOAD_POLL_MS)
    }

    private fun showResumeLoadingIfPossible() {
        val activity = currentActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.d(TAG_AO, "resume loading dialog skipped: no valid activity")
            return
        }
        dismissResumeLoading()
        resumeLoadingDialog = LoadingDialog(activity).also {
            Log.d(TAG_AO, "resume loading dialog shown")
            it.show()
        }
        resumeDialogHandler.postDelayed({
            if (resumeLoadingDialog != null) {
                Log.w(TAG_AO, "resume loading dialog timeout -> dismissing")
                dismissResumeLoading()
            }
        }, 7000L)
    }

    private fun dismissResumeLoading() {
        resumeDialogHandler.removeCallbacksAndMessages(null)
        resumeLoadingDialog?.dismiss()
        resumeLoadingDialog = null
        Log.d(TAG_AO, "resume loading dialog dismissed")
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        try {
            // Clean up reference if this was the current activity
            if (currentActivity == activity) {
                currentActivity = null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onActivityDestroyed")
        }
    }
}
