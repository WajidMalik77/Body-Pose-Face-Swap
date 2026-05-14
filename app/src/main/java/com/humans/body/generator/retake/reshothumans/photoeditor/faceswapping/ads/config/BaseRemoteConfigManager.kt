package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseRemoteConfigManager<T>(
    protected val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val remoteKey: String
) {
    protected var configData: T? = null
    private val onConfigAvailableCallbacks = mutableListOf<() -> Unit>()
    protected var isConfigListenerSet = false
    @Volatile
    private var isFetching = false

    fun fetchConfig() {
        if (configData != null) {
            Log.d("ConfigTrace", "fetchConfig skip key=$remoteKey reason=already_loaded")
            notifyConfigAvailable()
            return
        }
        if (isFetching) {
            Log.d("ConfigTrace", "fetchConfig skip key=$remoteKey reason=in_flight")
            return
        }
        isFetching = true
        Log.d("ConfigTrace", "fetchConfig start key=$remoteKey manager=${this::class.java.simpleName}")
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val json = firebaseRemoteConfig.getString(remoteKey)
                    Log.d("ConfigTrace",
                        "fetchConfig complete key=$remoteKey success=true jsonLength=${json.length}"
                    )
                    if (json.isNotBlank()) {
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                val parsedData = parseJson(json)
                                withContext(Dispatchers.Main) {
                                    configData = parsedData
                                    Log.d("ConfigTrace",
                                        "parse result key=$remoteKey parsedNull=${parsedData == null}"
                                    )
                                    postProcessParsedData(json)
                                    notifyConfigAvailable()
                                    isFetching = false
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "$remoteKey configuration parse error")
                                isFetching = false
                            }
                        }
                    } else {
                        Log.e("ConfigTrace", "$remoteKey JSON is blank")
                        isFetching = false
                    }
                } else {
                    Timber.e(task.exception, "Remote Config fetch failed for $remoteKey")
                    isFetching = false
                }
            }
            .addOnFailureListener {
                Timber.e(it, "Fetch and activate failed for $remoteKey")
                isFetching = false
            }
    }

    protected abstract fun parseJson(json: String): T?

    protected open fun postProcessParsedData(json: String) {}

    open fun setOnConfigAvailableListener(callback: () -> Unit) {
        onConfigAvailableCallbacks.add(callback)
        if (configData != null) callback()
    }

    fun getConfig(): T? {
        if (configData == null) fetchConfig()
        return configData
    }

    private fun notifyConfigAvailable() {
        if (onConfigAvailableCallbacks.isEmpty()) return
        onConfigAvailableCallbacks.forEach { it.invoke() }
    }
}
