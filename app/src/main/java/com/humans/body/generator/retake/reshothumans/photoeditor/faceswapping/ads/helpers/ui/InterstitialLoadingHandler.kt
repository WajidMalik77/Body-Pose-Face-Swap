package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.dialogs.LoadingDialog
import timber.log.Timber

class InterstitialLoadingHandler(private val activity: Activity) {

    private val handler = Handler(Looper.getMainLooper())
    private var adHandled = false

    fun showWithLoading(
        adUnitId: String,
        onLoadAd: (String, () -> Unit, (String) -> Unit) -> Unit,
        onShowAd: () -> Unit,
        onFailed: () -> Unit
    ) {
        val loadingDialog = LoadingDialog(activity)
        loadingDialog.show()

        val startTime = System.currentTimeMillis()

        // Timeout handler
        handler.postDelayed({
            if (!adHandled) {
                Timber.w("Ad load timeout reached")
                dismissAndProceed(loadingDialog, startTime) { onFailed() }
            }
        }, MAX_TIMEOUT)

        // Start ad load slightly later so the dialog can render first and avoid flash behavior.
        handler.postDelayed({
            onLoadAd(
                adUnitId,
                { // onLoaded
                    dismissAndProceed(loadingDialog, startTime) { onShowAd() }
                },
                { error -> // onFailed
                    dismissAndProceed(loadingDialog, startTime) {
                        Timber.e("Interstitial failed: $error")
                        onFailed()
                    }
                }
            )
        }, START_LOAD_DELAY)
    }

    private fun dismissAndProceed(
        dialog: LoadingDialog,
        startTime: Long,
        action: () -> Unit
    ) {
        if (adHandled) return

        val elapsed = System.currentTimeMillis() - startTime
        val remaining = MIN_LOADING_TIME - elapsed

        if (remaining > 0) {
            handler.postDelayed({
                if (!adHandled) {
                    adHandled = true
                    dialog.dismiss()
                    action()
                }
            }, remaining)
        } else {
            adHandled = true
            dialog.dismiss()
            action()
        }
    }

    companion object {
        private const val MIN_LOADING_TIME = 1500L
        private const val MAX_TIMEOUT = 7000L
        private const val START_LOAD_DELAY = 180L
    }
}
