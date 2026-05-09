package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.MyApplication.Companion.getAppContext
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants

class SharePref {
    companion object {
        private const val TAG = "SharePref"
        private lateinit var pref: SharedPreferences

        private fun initPref() {
            pref = getAppContext()
                .getSharedPreferences("Recovery_Pref", Context.MODE_PRIVATE)
        }

        fun putString(key: String, value: String) {
            if (!Companion::pref.isInitialized)
                initPref()
            pref.edit { putString(key, value) }
        }

        fun putBoolean(key: String, value: Boolean) {
            if (!Companion::pref.isInitialized)
                initPref()
            pref.edit { putBoolean(key, value) }
        }
        fun remove(key: String){
            if (!Companion::pref.isInitialized)
                initPref()
            pref.edit { remove(key) }
        }

        fun getString(key: String , default: String): String {
            if (!Companion::pref.isInitialized)
                initPref()
            val value = pref.getString(key, default) ?: default

            // Some environments provide only one of these RC keys.
            // Keep API auth stable by falling back across both keys.
            if (key == Constants.new_version_key && value.isBlank()) {
                val fallback = pref.getString(Constants.api_key, default) ?: default
                if (fallback.isNotBlank()) {
                    Log.d(TAG, "Using fallback api_key for new_version_key")
                    return fallback
                }
            }

            if (key == Constants.api_key && value.isBlank()) {
                val fallback = pref.getString(Constants.new_version_key, default) ?: default
                if (fallback.isNotBlank()) {
                    Log.d(TAG, "Using fallback new_version_key for api_key")
                    return fallback
                }
            }

            return value
        }

        fun getBoolean(key: String , default: Boolean): Boolean {
            if (!Companion::pref.isInitialized)
                initPref()
            return pref.getBoolean(key, default)
        }

        fun getInt(key: String, i: Int) : Int{
            if (!Companion::pref.isInitialized)
                initPref()
            return pref.getInt(key, i)
        }

        fun putInt(key: String , value:Int){
            if (!Companion::pref.isInitialized)
                initPref()
            pref.edit().putInt(key, value).apply()
        }
    }
}
