package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.update

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class ImmediateAppUpdater(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager? = runCatching {
        AppUpdateManagerFactory.create(activity)
    }.getOrElse {
        Log.w(TAG, "Failed to create AppUpdateManager", it)
        null
    }

    fun checkAndLaunchImmediateUpdate(
        launcher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
        onUpdateFlowLaunched: () -> Unit,
        onNoImmediateUpdate: () -> Unit
    ) {
        try {
            val manager = appUpdateManager ?: run {
                onNoImmediateUpdate()
                return
            }
            manager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (shouldRunImmediateUpdate(appUpdateInfo)) {
                        startImmediateUpdate(appUpdateInfo, launcher, onUpdateFlowLaunched, onNoImmediateUpdate)
                    } else {
                        onNoImmediateUpdate()
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Update check failed", it)
                    onNoImmediateUpdate()
                }
        } catch (t: Throwable) {
            Log.w(TAG, "Unexpected crash while checking immediate update", t)
            onNoImmediateUpdate()
        }
    }

    fun resumeIfImmediateUpdateInProgress(
        launcher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
        onUpdateFlowLaunched: () -> Unit
    ) {
        try {
            val manager = appUpdateManager ?: return
            manager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        startImmediateUpdate(appUpdateInfo, launcher, onUpdateFlowLaunched) {}
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Failed to resume in-progress immediate update", it)
                }
        } catch (t: Throwable) {
            Log.w(TAG, "Unexpected crash while resuming immediate update", t)
        }
    }

    private fun shouldRunImmediateUpdate(appUpdateInfo: AppUpdateInfo): Boolean {
        return appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    }

    private fun startImmediateUpdate(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
        onUpdateFlowLaunched: () -> Unit,
        onFailure: () -> Unit
    ) {
        val manager = appUpdateManager ?: run {
            onFailure()
            return
        }
        runCatching {
            manager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                com.google.android.play.core.appupdate.AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }.onSuccess {
            onUpdateFlowLaunched()
        }.onFailure {
            Log.w(TAG, "Failed to launch immediate update flow", it)
            onFailure()
        }
    }

    companion object {
        private const val TAG = "ImmediateAppUpdater"
    }
}
