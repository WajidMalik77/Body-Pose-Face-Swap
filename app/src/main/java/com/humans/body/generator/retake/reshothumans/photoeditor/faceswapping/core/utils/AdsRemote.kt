package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Is_full_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Isapplication_app_open_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Isboarding_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Ishome_inter_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Ishome_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Islang_inter_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Islang_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Ispurchase_inter_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.Issplash_app_open_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.app_open
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.application_app_open_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.boarding_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.full_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.home_inter_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.home_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.interstitial
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.interstitial_counter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.lang_inter_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.lang_native_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.native
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.purchase_inter_ad_key
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.splash_app_open_ad_key
import com.zeugmasolutions.localehelper.Locales
import java.util.Locale

fun getRemoteValues(
    key: String,
    adName: String,
    counterKey: String = "",
    adTypeForDebug: String,
    callbacks: (Boolean, String, Int) -> Unit
) {
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600 // 1 hour
    }
    val remoteConfig = Firebase.remoteConfig
    remoteConfig.setConfigSettingsAsync(configSettings)

    val remoteConfigDefaults: Map<String, Any> = mapOf(

        application_app_open_ad_key to "",
        Isapplication_app_open_ad_key to false,

        splash_app_open_ad_key to "",
        Issplash_app_open_ad_key to false,

        lang_native_ad_key to "",
        Islang_native_ad_key to false,

        lang_inter_ad_key to "",
        Islang_inter_ad_key to false,

        purchase_inter_ad_key to "",
        Ispurchase_inter_ad_key to false,

        home_inter_ad_key to "",
        Ishome_inter_ad_key to false,

        boarding_native_ad_key to "",
        Isboarding_native_ad_key to false,

        home_native_ad_key to "",
        Ishome_native_ad_key to false,

        full_native_ad_key to "",
        Is_full_native_ad_key to false,

        interstitial_counter to 3,


        )
    remoteConfig.setDefaultsAsync(remoteConfigDefaults)
    Log.d("hello", "DEBUG = ${BuildConfig.DEBUG}")

    if (BuildConfig.DEBUG) {
        Log.d("hello", "debug")

        callbacks(
            true, when (adTypeForDebug) {
                interstitial -> "ca-app-pub-3940256099942544/1033173712"
                native -> "ca-app-pub-3940256099942544/2247696110"
                app_open -> "ca-app-pub-3940256099942544/9257395921"
                else -> ""
            }, 3
        )
    } else {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                Log.d("hello", "fetchAndActivate success: ${task.isSuccessful}")
                // Always read values — Firebase SDK falls back to cached/default values on failure
                val boolValue = remoteConfig.getBoolean(key)
                val adId = if (boolValue) remoteConfig.getString(adName) else ""
                val counter = remoteConfig.getLong(counterKey).toInt()
                Log.d("hello", "$key : $boolValue , id : $adId")
                callbacks(boolValue, adId, counter)
            }
            .addOnFailureListener {
                Log.e("hello", "fetchAndActivate failure: $it")
                // Still try cached/default values instead of disabling ads entirely
                val boolValue = remoteConfig.getBoolean(key)
                val adId = if (boolValue) remoteConfig.getString(adName) else ""
                val counter = remoteConfig.getLong(counterKey).toInt()
                callbacks(boolValue, adId, counter)
            }
    }
}

data class OnboardingItem(
    val type: Int,
    val title: String? = null,
    val description: String? = null,
    val imageRes: Int? = null
)

const val TYPE_DATA = 0
const val TYPE_AD = 1

data class Lang(val res: Int, val name: String, val locale: Locale)

fun Context.getLangData() = arrayListOf(
    Lang(R.drawable.flag_us, "English", Locales.English),
    Lang(R.drawable.flag_hi, "Hindi", Locales.Hindi),
    Lang(R.drawable.flag_es, "Spanish", Locales.Spanish),
    Lang(R.drawable.flag_fr, "French", Locales.French),
    Lang(R.drawable.flag_pt, "Portuguese", Locales.Portuguese),
    Lang(R.drawable.flag_germany, "German", Locales.German),
    Lang(R.drawable.flag_ja, "Japanese", Locales.Japanese),
    Lang(R.drawable.flag_ar, "Arabic", Locales.Arabic),

//    Lang(R.drawable.flag_china, "Chinese (Simplified)", Locales.French),

)

fun Activity.openGooglePrivacy() {
    try {
        val url =
            "https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldl=en_GB&ldo=0&ldt=buyertos"
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    } catch (e: Exception) {
        // Catch Exception here
    }
}

fun Context.privacyPolicy(url: String=  "https://pps66.wordpress.com/2022/11/22/privacy-policy/") {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    } catch (e: Exception) {
        // Catch Exception here
    }
}

fun Context.getMenuData() = arrayListOf(
    Lang(R.drawable.premium, getString(R.string.unlock_premium), Locales.English),
    Lang(R.drawable.language, getString(R.string.language), Locales.English),
    Lang(R.drawable.share_app, getString(R.string.share_app), Locales.Spanish),
    Lang(R.drawable.rate_us, getString(R.string.rate_us), Locales.French),
    Lang(R.drawable.feedback, getString(R.string.feedback), Locales.Portuguese),
    Lang(R.drawable.terms_condition, getString(R.string.terms_amp_conditions), Locales.Portuguese),
    Lang(R.drawable.privacy_policy, getString(R.string.privacy_policy), Locales.Japanese),
)


// Extension: Rate Us
fun Context.openRateUs(packageName: String = this.packageName) {
    try {
        // Open Play Store app
        val uri = "market://details?id=$packageName".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.vending")
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback to browser
        val uri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "Unable to open Play Store", Toast.LENGTH_SHORT).show()
    }
}

// Extension: Send Feedback
fun Context.openFeedback(
    email: String = "ideasfreeapps@gmail.com",
    subject: String = getString(R.string.app_name),
    chooserTitle: String = "Send Feedback"
) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        startActivity(Intent.createChooser(intent, chooserTitle))
    } catch (e: Exception) {
        Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
    }
}


fun shareApp(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Download this great app at: https://play.google.com/store/apps/details?id=${context.packageName}"
        )
        shareIntent.type = "text/plain"
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}