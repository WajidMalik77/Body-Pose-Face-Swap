package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber


import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app.AppInitializer
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app.AppOpenAdLifecycleManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app.BillingManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.BillingUtilsIAP
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.RemoteConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SubscriptionBilling
import dagger.hilt.android.HiltAndroidApp
import com.zeugmasolutions.localehelper.LocaleAwareApplication
import javax.inject.Inject

@HiltAndroidApp
open class MyApplication : LocaleAwareApplication(), LifecycleObserver,
    Application.ActivityLifecycleCallbacks {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
        var isResume = false
        fun getAppContext(): Context = mContext.applicationContext
    }

    @Inject
    lateinit var appInitializer: AppInitializer
    @Inject
    lateinit var billingManager: BillingManager
    @Inject
    lateinit var appOpenAdManager: AppOpenAdLifecycleManager

    override fun onCreate() {
        super.onCreate()
        if (com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Log.d("AppInitTrace", "MyApplication.onCreate entered")

        mContext = this
        FirebaseApp.initializeApp(this)
        Log.d("AppInitTrace", "Firebase initialized")
        RemoteConfig.getRCValues(this) {
            Log.d("AppInitTrace", "RemoteConfig.getRCValues() callback invoked")
        }
        Log.d("AppInitTrace", "RemoteConfig.getRCValues() called")
        appInitializer.initialize()
        Log.d("AppInitTrace", "appInitializer.initialize() called")
        billingManager.initialize()
        Log.d("AppInitTrace", "billingManager.initialize() called")
        appOpenAdManager.initialize()
        Log.d("AppInitTrace", "appOpenAdManager.initialize() called")
        SubscriptionBilling(this)
        BillingUtilsIAP(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenAdManager)
        registerActivityLifecycleCallbacks(appOpenAdManager)
        Log.d("AppInitTrace", "AppOpen lifecycle observers registered")

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d("AppInitTrace", "MyApplication lifecycle observers registered")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "FCM Registration Token: $token")
        }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { mContext = activity }
    override fun onActivityStarted(activity: Activity) { mContext = activity }
    override fun onActivityResumed(activity: Activity) {
        mContext = activity
    }
    override fun onActivityPaused(activity: Activity) { mContext = activity }
    override fun onActivityStopped(activity: Activity) { mContext = activity }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { mContext = activity }
    override fun onActivityDestroyed(activity: Activity) { mContext = activity }
}
