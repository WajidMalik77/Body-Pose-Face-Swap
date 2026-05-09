package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants

class PrefUtil(private val context: Context) {
    fun setInt(key: String?, value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit {
            putInt(key, value)
        }
    }

    fun getInt(key: String?, defValue: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getInt(key, defValue)
    }

    fun setString(key: String?, value: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit {
            putString(key, value)
        }
    }

    fun getString(key: String?): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getString(key, "null")
    }

    fun setBool(key: String?, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit {
            putBoolean(key, value)
        }
        if (key == "is_premium") {
            context.getSharedPreferences("app_ads", Context.MODE_PRIVATE).edit {
                putBoolean("is_premium", value)
            }
        }
    }

    fun getBool(key: String?): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(key, false)
    }

    fun getBool(key: String?, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(key, defaultValue)
    } //    public boolean getIsLinearLayout(String key) {

    companion object {
        val PREFS_NAME = "my_prefs"
        const val premiumKey = "PREMIUM"
        const val premiumCheck = "CHECK"
        fun setPremiumString(value: String, context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            prefs.edit {
                putString(premiumKey, value)
            }
        }

        fun getPremium(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return prefs.getString(premiumKey, "")
        }

        fun setPremium(context: Context, value: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            prefs.edit {
                putBoolean(premiumCheck, value)
                putBoolean("is_premium", value)
            }
            context.getSharedPreferences("app_ads", Context.MODE_PRIVATE).edit {
                putBoolean("is_premium", value)
            }
            context.getSharedPreferences(AdsConstants.LifeTimePref, Context.MODE_PRIVATE).edit {
                putBoolean("premium", value)
            }
            if (value) {
                hideAdsImmediately(context)
            }
        }

        fun isPremium(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val isSubscribed = prefs.getBoolean(premiumCheck, false)
                    || prefs.getBoolean("is_premium", false)
                    || context.getSharedPreferences("app_ads", Context.MODE_PRIVATE)
                        .getBoolean("is_premium", false)
            val isLifetime = context
                .getSharedPreferences(AdsConstants.LifeTimePref, Context.MODE_PRIVATE)
                .getBoolean("premium", false)
            return isSubscribed || isLifetime
        }

        private fun hideAdsImmediately(context: Context) {
            val activity = context.findActivity() ?: return

            // Hide common ad wrapper sections used across activities/fragments.
            listOf(
                "llBannerTop",
                "llBannerBottom",
                "llNativeTop",
                "llNativeCenter",
                "llNativeBottom"
            ).forEach { name ->
                val id = activity.resources.getIdentifier(name, "id", activity.packageName)
                if (id != 0) {
                    activity.findViewById<View>(id)?.visibility = View.GONE
                }
            }

            // Clear/detach currently attached ad + shimmer views.
            listOf(
                "admob_banner",
                "admob_native",
                "banner_ad_shimmer",
                "native_ad_shimmer"
            ).forEach { name ->
                val id = activity.resources.getIdentifier(name, "id", activity.packageName)
                if (id != 0) {
                    val v = activity.findViewById<View>(id)
                    (v as? ViewGroup)?.removeAllViews()
                    v?.visibility = View.GONE
                }
            }
        }

        private tailrec fun Context.findActivity(): android.app.Activity? = when (this) {
            is android.app.Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
    }
}
