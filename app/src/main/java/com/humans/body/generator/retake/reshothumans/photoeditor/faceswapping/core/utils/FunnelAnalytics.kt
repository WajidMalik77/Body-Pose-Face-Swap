package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants

object FunnelAnalytics {

    fun isNewUser(context: Context): Boolean {
        val prefs = context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
        return !prefs.getBoolean(AdsConstants.isFirstTime, false)
    }

    fun userPrefix(context: Context): String = if (isNewUser(context)) "new_user" else "old_user"

    fun logScreenEvent(context: Context, screen: String, event: String) {
        val eventName = "${userPrefix(context)}_${screen}_${event}"
        Firebase.analytics.logEvent(eventName) {}
    }

    fun logHomeClick(context: Context, target: String) {
        val eventName = "${userPrefix(context)}_home_to_${target}"
        Firebase.analytics.logEvent(eventName) {}
    }
}
