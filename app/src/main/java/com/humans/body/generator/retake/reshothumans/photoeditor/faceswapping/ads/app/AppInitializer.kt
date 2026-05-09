package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.AdConfigInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val firebaseInitializer: FirebaseInitializer,
    private val adConfigInitializer: AdConfigInitializer,
    private val adsManager: AdsManager,
    private val appOpenAdLifecycleManager: AppOpenAdLifecycleManager
) {
    companion object {
        private const val TAG_INIT = "AppInitTrace"
    }
    private val initializationState = AtomicBoolean(false)

    fun initialize(force: Boolean = false) {
        Log.d(TAG_INIT, "AppInitializer.initialize(force=$force) called")
        if (!force && !initializationState.compareAndSet(false, true)) {
            Log.d(TAG_INIT, "AppInitializer skipped: already initializing")
            Timber.w("Initialization already in progress")
            return
        }

        // Firebase init must never crash app
        try {
            firebaseInitializer.initialize()
            Log.d(TAG_INIT, "firebaseInitializer.initialize() success")
        } catch (e: Exception) {
            Log.e(TAG_INIT, "firebaseInitializer.initialize() failed", e)
            Timber.e(e, "Firebase initialization failed")
        }

        CoroutineScope(
            Dispatchers.Default + SupervisorJob()
        ).launch {
            try {
                val configResult = runCatching {
                    adConfigInitializer.preloadConfigs(force)
                }
                Log.d(TAG_INIT, "adConfigInitializer.preloadConfigs() invoked")

                adConfigInitializer.setListener(
                    onReady = {
                        Log.d(TAG_INIT, "Ad config ready callback fired")
                    },
                    onFailed = {
                        Log.w(TAG_INIT, "Ad config failed callback fired")
                        Timber.w("Ad config initialization failed, skipping splash app-open preload")
                    }
                )

                runCatching {
                    withContext(Dispatchers.IO) {
                        FirebaseRemoteConfig.getInstance()
                            .fetchAndActivate()
                            .await()
                    }
                }.onFailure {
                    Timber.w(it, "Remote Config fetchAndActivate failed (likely offline)")
                }

                // Log failures, but NEVER crash app
                configResult.exceptionOrNull()?.let {
                    Timber.e(it, "Config preload failed (offline or service unavailable)")
                }

                Log.d(TAG_INIT, "Skipping app-start ad init; splash consent flow will initialize ads when allowed")
            } catch (e: Exception) {
                Log.e(TAG_INIT, "AppInitializer background initialization failed", e)
                Timber.e(e, "App initialization failed")
            } finally {
                Log.d(TAG_INIT, "AppInitializer background initialization finished")
                initializationState.set(false)
            }
        }
    }
}
