package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.AdConfigInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val firebaseInitializer: FirebaseInitializer,
    private val adConfigInitializer: AdConfigInitializer) {
    private val initializationState = AtomicBoolean(false)

    fun initialize(force: Boolean = false) {
        Timber.d("AppInitializer.initialize(force=$force) called")
        if (!force && !initializationState.compareAndSet(false, true)) {
            Timber.d("AppInitializer skipped: already initializing")
            Timber.w("Initialization already in progress")
            return
        }

        try {
            firebaseInitializer.initialize()
            Timber.d("firebaseInitializer.initialize() success")
        } catch (e: Exception) {
            Timber.d("firebaseInitializer.initialize() failed: $e")
            Timber.e(e, "Firebase initialization failed")
        }

        CoroutineScope(
            Dispatchers.Default + SupervisorJob()
        ).launch {
            try {
                val configResult = runCatching {
                    adConfigInitializer.preloadConfigs(force)
                }
                Timber.d("adConfigInitializer.preloadConfigs() invoked")

                adConfigInitializer.setListener(
                    onReady = {
                        Timber.d("Ad config ready callback fired")
                    },
                    onFailed = {
                        Timber.d("Ad config failed callback fired")
                        Timber.w("Ad config initialization failed, skipping splash app-open preload")
                    }
                )

                // Log failures, but NEVER crash app
                configResult.exceptionOrNull()?.let {
                    Timber.e(it, "Config preload failed (offline or service unavailable)")
                }

                Timber.d("Skipping app-start ad init; splash consent flow will initialize ads when allowed")
            } catch (e: Exception) {
                Timber.d("AppInitializer background initialization failed: $e")
                Timber.e(e, "App initialization failed")
            } finally {
                Timber.d("AppInitializer background initialization finished")
                initializationState.set(false)
            }
        }
    }
}