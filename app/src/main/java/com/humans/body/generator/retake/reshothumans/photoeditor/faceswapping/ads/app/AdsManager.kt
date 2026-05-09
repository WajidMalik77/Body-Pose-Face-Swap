package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    private val context: Application,
    private val premiumRepository: PremiumRepository
) {
    private val initDeferred = CompletableDeferred<Unit>()
    @Volatile
    private var initStarted = false
    fun initializeIfNeeded() {
        if (premiumRepository.isPremiumUser()) {
            initDeferred.complete(Unit) // Unblock anyone waiting
            return
        }

        if (initStarted) return

        synchronized(this) {
            if (initStarted) return
            initStarted = true
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Handler(Looper.getMainLooper()).post {
                    MobileAds.initialize(context) { status ->
                        Timber.i("MobileAds initialized: $status")
                        initDeferred.complete(Unit)
                    }
                }
            }.onFailure { e ->
                Timber.e(e, "MobileAds initialization failed")
                initDeferred.completeExceptionally(e)
            }
        }
    }

    suspend fun awaitInitialization() {
        initDeferred.await()
    }
}