package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.content.Context
import androidx.core.content.edit

object TrialManager {

    private const val PREF = "trial_prefs"
    private const val KEY_COUNT = "use_count"
    private const val LIMIT = 1

    fun canUse(context: Context): Boolean {
        val count = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_COUNT, 0)
        return count < LIMIT
    }

    fun increase(context: Context) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val count = pref.getInt(KEY_COUNT, 0) + 1
        pref.edit { putInt(KEY_COUNT, count) }
    }

//    fun remaining(context: Context): Int {
//        val used = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
//            .getInt(KEY_COUNT, 0)
//        return LIMIT - used
//    }
}
