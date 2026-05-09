package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdData
import kotlin.text.isEmpty
import kotlin.text.toInt

class RemoteConfig {
    companion object {
        private const val TAG = "RCKEY"
        var adData = AdData()
        private fun logFetchedApiKeys(source: String, apiKey: String, newVersionKey: String) {
            Log.d(
                "RC_API_KEY",
                "$source api_key=$apiKey len=${apiKey.length}"
            )
            Log.d(
                "RC_API_KEY",
                "$source new_version_key=$newVersionKey len=${newVersionKey.length}"
            )
        }

        fun getRCValues(context: Context, callback: () -> Unit) {
            Log.e("RC_FLOW", "getRCValues() CALLED — isDebug=${BuildConfig.DEBUG}")
            val remoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0L
            }
            remoteConfig.setConfigSettingsAsync(configSettings)

            remoteConfig.fetchAndActivate()

                .addOnCompleteListener { task ->
                    Log.e(
                        "RC_FLOW",
                        "fetchAndActivate complete — isSuccessful=${task.isSuccessful} | isDebug=${BuildConfig.DEBUG}"
                    )
                    if (task.isSuccessful) {
                        Log.e(
                            "RC_FLOW",
                            "✅ Fetch succeeded — going into ${if (BuildConfig.DEBUG) "DEBUG/debugKeys()" else "RELEASE/fetchRcKeys()"} branch"
                        )

                        if (BuildConfig.DEBUG) {
                            val fetchedApiKey = remoteConfig.getString(Constants.api_key)
                            val fetchedNewVersionKey = remoteConfig.getString(Constants.new_version_key)
                            logFetchedApiKeys(
                                source = "DEBUG_FETCH",
                                apiKey = fetchedApiKey,
                                newVersionKey = fetchedNewVersionKey
                            )


                            SharePref.putString(
                                Constants.api_key,
                                fetchedApiKey  // "api_key"
                            )

                            SharePref.putString(
                                Constants.new_version_key,
                                fetchedNewVersionKey  // "api_key"
                            )



                            SharePref.putString(
                                Constants.AdmobNativeId,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_language_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_onboarding_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_language_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_language_cta_text_color,
                                "#ffffff"
                            )


                            SharePref.putString(
                                Constants.native_onboarding_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_onboarding_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_reshape_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_reshape_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_save_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_save_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_faceswap_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_faceswap_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_ai_resize_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_ai_resize_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_image_to_image_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_image_to_image_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_image_varition_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_image_varition_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_crop_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_crop_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_restyle_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_restyle_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_upscale_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_upscale_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_photo_editor_cta_color,
                                "#000000"
                            )
                            SharePref.putString(
                                Constants.native_photo_editor_cta_text_color,
                                "#ffffff"
                            )
                            SharePref.putString(
                                Constants.native_reshape_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.banner_select_pose_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.banner_bg_remover_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_ai_size_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_crop_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_image_to_image_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_image_varition_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_photo_editor_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_reshape_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_restyle_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_upscale_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_faceswap_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.banner_face_style_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.banner_text_to_image_key,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.native_faceswap_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_ai_resize_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_image_to_image_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_restyle_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_upscale_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_image_varition_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_crop_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_photo_editor_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_save_key,
                                context.getString(R.string.admob_native_id)
                            )

                            SharePref.putString(
                                Constants.native_apply_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.native_onboarding_ful_key,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.Lang_Interstitial_key,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putString(
                                Constants.rewardedAdId,
                                context.getString(R.string.admobRewardAd)
                            )
                            SharePref.putString(
                                Constants.Purchase_Interstitial_key,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putString(
                                Constants.AdmobInterstitialId,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putBoolean(
                                Constants.IsLang_Interstitial_key,
                                true
                            )
                            SharePref.putBoolean(
                                Constants.IsRewardedAdEnable,
                                true
                            )
                            SharePref.putBoolean(
                                Constants.Is_Purchase_Interstitial_key,
                                true
                            )
                            SharePref.putBoolean(
                                Constants.Ishome_inter_ad_key,
                                true
                            )
//                            SharePref.putBoolean(
//                                Constants.IsLang_Interstitial_key,
//                                remoteConfig.getBoolean(Constants.IsLang_Interstitial_key)
//                            )
                            SharePref.putString(
                                Constants.AdmobNativeIdHigh,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashNativeId,
                                context.getString(R.string.admob_native_id)
                            )
                            SharePref.putString(
                                Constants.AdmobInterId,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putString(
                                Constants.AdmobInterIdHigh,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashInterId,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashInterIdHigh,
                                context.getString(R.string.admob_interstitial_id)
                            )
                            SharePref.putString(
                                Constants.AdmobBannerId,
                                context.getString(R.string.admob_banner_id)
                            )
                            SharePref.putString(
                                Constants.AdmobBannerIdHigh,
                                context.getString(R.string.admob_banner_id)
                            )
//                            SharePref.putString(
//                                Constants.AdmobCollapsibleBannerId,
//                                context.getString(R.string.admob_collapsible_banner_id)
//                            )
//                            SharePref.putString(
//                                Constants.AdmobCollapsibleBannerIdHigh,
//                                context.getString(R.string.admob_collapsible_banner_id)
//                            )

                            SharePref.putString(
                                Constants.AdmobOpenAdId,
                                context.getString(R.string.admob_openAd_id)
                            )
                            SharePref.putString(
                                Constants.AdmobOpenAdIdHigh,
                                context.getString(R.string.admob_openAd_id)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashOpenId,
                                context.getString(R.string.admob_openAd_id)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashOpenIdHigh,
                                context.getString(R.string.admob_openAd_id)
                            )
                            SharePref.putString(
                                Constants.CappingCounter,
                                "2"
                            )

                            SharePref.putBoolean(Constants.native_saved, true)
                            SharePref.putBoolean(Constants.rectangle_banner_saved, true)
                            SharePref.putString(
                                Constants.native_saved_key,
                                "ca-app-pub-3940256099942544/2247696110"
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_saved_key,
                                "ca-app-pub-3940256099942544/2247696110"
                            )
                            SharePref.putString(Constants.native_saved_cta_color, "#000000")
                            SharePref.putString(Constants.native_saved_cta_text_color, "#ffffff")
                            SharePref.putInt(Constants.native_saved_width, 120)
                            SharePref.putInt(Constants.native_saved_height, 48)
                            SharePref.putString(Constants.AdTypeNative_saved, "s")
                            SharePref.putString(Constants.CtaTypeNative_saved, "b")



                            Log.e("RC_FLOW", "--- DEBUG hardcoded values being set ---")
                            Log.e("RC_FLOW", "native_reshape will be set to: TRUE (hardcoded)")
                            Log.e(
                                "RC_FLOW",
                                "rectangle_banner_reshape will be set to: TRUE (hardcoded)"
                            )
                            Log.e(
                                "RC_FLOW",
                                "native_reshape_key will be set to: '${context.getString(R.string.admob_native_id)}'"
                            )
                            Log.e(
                                "RC_FLOW",
                                "rectangle_banner_reshape_key will be set to: '${context.getString(R.string.admob_banner_id)}'"
                            )


                            debugKeys()
                        } else {

                            Log.e("RC_FLOW", "--- RELEASE — fetching from Firebase RC ---")
                            fetchRcKeys(remoteConfig)

                            SharePref.putString(
                                Constants.api_key,
                                remoteConfig.getString(Constants.api_key)  // "api_key"
                            )

                            SharePref.putString(
                                Constants.new_version_key,
                                remoteConfig.getString(Constants.new_version_key)  // "api_key"
                            )

                            SharePref.putString(
                                Constants.AdmobCtaTextColor,
                                remoteConfig.getString(Constants.AdmobCtaTextColor)
                            )
                            SharePref.putString(
                                Constants.AdmobCtaColor,
                                remoteConfig.getString(Constants.AdmobCtaColor)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashNativeId,
                                remoteConfig.getString(Constants.AdmobSplashNativeId)
                            )

                            SharePref.putString(
                                Constants.AdmobNativeId,
                                remoteConfig.getString(Constants.AdmobNativeId)
                            )
                            SharePref.putString(
                                Constants.AdmobNativeIdHigh,
                                remoteConfig.getString(Constants.AdmobNativeIdHigh)
                            )
                            SharePref.putString(
                                Constants.AdmobInterId,
                                remoteConfig.getString(Constants.AdmobInterId)
                            )
                            SharePref.putString(
                                Constants.AdmobInterIdHigh,
                                remoteConfig.getString(Constants.AdmobInterIdHigh)
                            )
                            SharePref.putString(
                                Constants.Lang_Interstitial_key,
                                remoteConfig.getString(Constants.Lang_Interstitial_key)
                            )
                            SharePref.putString(
                                Constants.rewardedAdId,
                                remoteConfig.getString(Constants.rewardedAdId)
                            )
                            SharePref.putString(
                                Constants.Purchase_Interstitial_key,
                                remoteConfig.getString(Constants.Purchase_Interstitial_key)
                            )
                            SharePref.putString(
                                Constants.AdmobInterstitialId,
                                remoteConfig.getString(Constants.AdmobInterstitialId)
                            )
                            SharePref.putBoolean(
                                Constants.IsLang_Interstitial_key,
                                remoteConfig.getBoolean(Constants.IsLang_Interstitial_key)
                            )
                            SharePref.putBoolean(
                                Constants.IsRewardedAdEnable,
                                remoteConfig.getBoolean(Constants.IsRewardedAdEnable)
                            )
                            SharePref.putBoolean(
                                Constants.Is_Purchase_Interstitial_key,
                                remoteConfig.getBoolean(Constants.Is_Purchase_Interstitial_key)
                            )
                            SharePref.putBoolean(
                                Constants.Ishome_inter_ad_key,
                                remoteConfig.getBoolean(Constants.Ishome_inter_ad_key)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashInterId,
                                remoteConfig.getString(Constants.AdmobSplashInterId)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashInterIdHigh,
                                remoteConfig.getString(Constants.AdmobSplashInterIdHigh)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashOpenId,
                                remoteConfig.getString(Constants.AdmobSplashOpenId)
                            )
                            SharePref.putString(
                                Constants.AdmobSplashOpenIdHigh,
                                remoteConfig.getString(Constants.AdmobSplashOpenIdHigh)
                            )
                            SharePref.putString(
                                Constants.AdmobBannerId,
                                remoteConfig.getString(Constants.AdmobBannerId)
                            )
                            SharePref.putString(
                                Constants.AdmobBannerIdHigh,
                                remoteConfig.getString(Constants.AdmobBannerIdHigh)
                            )
                            SharePref.putString(
                                Constants.AdmobCollapsibleBannerId,
                                remoteConfig.getString(Constants.AdmobCollapsibleBannerId)
                            )
                            SharePref.putString(
                                Constants.AdmobCollapsibleBannerIdHigh,
                                remoteConfig.getString(Constants.AdmobCollapsibleBannerIdHigh)
                            )
                            SharePref.putString(
                                Constants.AdmobOpenAdId,
                                remoteConfig.getString(Constants.AdmobOpenAdId)
                            )
                            SharePref.putString(
                                Constants.AdmobOpenAdIdHigh,
                                remoteConfig.getString(Constants.AdmobOpenAdIdHigh)
                            )

                            SharePref.putString(
                                Constants.CappingCounter,
                                remoteConfig.getString(Constants.CappingCounter)
                            )


                            SharePref.putBoolean(Constants.native_saved, true)
                            SharePref.putBoolean(Constants.rectangle_banner_saved, true)
                            SharePref.putString(
                                Constants.native_saved_key,
                                "ca-app-pub-3940256099942544/2247696110"
                            )
                            SharePref.putString(
                                Constants.rectangle_banner_saved_key,
                                "ca-app-pub-3940256099942544/2247696110"
                            )
                            SharePref.putString(Constants.native_saved_cta_color, "#000000")
                            SharePref.putString(Constants.native_saved_cta_text_color, "#ffffff")
                            SharePref.putInt(Constants.native_saved_width, 120)
                            SharePref.putInt(Constants.native_saved_height, 48)
                            SharePref.putString(Constants.AdTypeNative_saved, "s")
                            SharePref.putString(Constants.CtaTypeNative_saved, "b")


                        }
                        setAdData()
                        callback.invoke()
                        Log.d(TAG, "debugKeys: done")
                    } else {
                        // Fetch failed — still load cached/default values so ads can work
                        Log.d(TAG, "fetchAndActivate not successful, using cached values")
                        if (!BuildConfig.DEBUG) fetchRcKeys(remoteConfig)
                        setAdData()
                        callback.invoke()
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "fetchValuesFromRc: $it")
                    // Still populate adData from cached/default values before invoking callback
                    if (!BuildConfig.DEBUG) fetchRcKeys(remoteConfig)
                    setAdData()
                    callback.invoke()
                }
        }

        private fun debugKeys() {
            SharePref.putBoolean(Constants.isAdmobEnable, true)
            SharePref.putBoolean(Constants.IsOpenAdEnable, true)
            SharePref.putBoolean(Constants.IsSplashOpenEnable, true)
            SharePref.putBoolean(Constants.IsSplashInterEnable, true)

            SharePref.putBoolean(Constants.isCollapsible, true)
            SharePref.putBoolean(Constants.isBannerCollapsibleDown, true)

            //Banner admob ids
            SharePref.putBoolean(Constants.banner_main, true)

            // native admob ids
            SharePref.putBoolean(Constants.native_language, true)
            SharePref.putBoolean(Constants.native_reshape, true)
            SharePref.putBoolean(Constants.banner_select_pose, true)
            SharePref.putBoolean(Constants.banner_bg_remover, true)
            SharePref.putBoolean(Constants.rectangle_banner_ai_resize, true)
            SharePref.putBoolean(Constants.rectangle_banner_crop, true)
            SharePref.putBoolean(Constants.rectangle_banner_image_to_image, true)
            SharePref.putBoolean(Constants.rectangle_banner_image_varition, true)
            SharePref.putBoolean(Constants.rectangle_banner_photo_editor, true)
            SharePref.putBoolean(Constants.rectangle_banner_reshape, true)
            SharePref.putBoolean(Constants.rectangle_banner_restyle, true)
            SharePref.putBoolean(Constants.rectangle_banner_upscale, true)
            SharePref.putBoolean(Constants.rectangle_banner_faceswape, true)
            SharePref.putBoolean(Constants.banner_face_style, true)
            SharePref.putBoolean(Constants.banner_text_to_image, true)
            SharePref.putBoolean(Constants.native_faceswap, true)
            SharePref.putBoolean(Constants.native_ai_resize, true)
            SharePref.putBoolean(Constants.native_image_to_image, true)
            SharePref.putBoolean(Constants.native_restyle, true)
            SharePref.putBoolean(Constants.native_upscale, true)
            SharePref.putBoolean(Constants.native_image_varition, true)
            SharePref.putBoolean(Constants.native_crop, true)
            SharePref.putBoolean(Constants.native_photo_editor, true)
            SharePref.putBoolean(Constants.native_save, true)
            SharePref.putBoolean(Constants.native_apply, true)
            SharePref.putString(
                Constants.native_language_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_onboarding_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_language_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_language_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_onboarding_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_onboarding_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_reshape_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_reshape_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_save_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_save_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_faceswap_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_faceswap_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_ai_resize_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_ai_resize_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_image_to_image_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_image_to_image_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_image_varition_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_image_varition_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_crop_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_crop_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_restyle_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_restyle_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_upscale_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_upscale_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_photo_editor_cta_color,
                "#000000"
            )
            SharePref.putString(
                Constants.native_photo_editor_cta_text_color,
                "#ffffff"
            )
            SharePref.putString(
                Constants.native_reshape_key,
                "ca-app-pub-3940256099942544/2247696110"
            )

            SharePref.putString(
                Constants.native_faceswap_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_ai_resize_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_image_to_image_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_restyle_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_upscale_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_image_varition_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_crop_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_photo_editor_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_save_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_apply_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.native_onboarding_ful_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putBoolean(Constants.native_onboarding, true)
            SharePref.putBoolean(Constants.is_native_onboarding_full, true)
            SharePref.putBoolean(Constants.native_home, true)
            SharePref.putInt(Constants.native_width, 120)
            SharePref.putInt(Constants.native_language_width, 120)
            SharePref.putInt(Constants.native_language_height, 48)
            SharePref.putInt(Constants.native_onboarding_width, 120)
            SharePref.putInt(Constants.native_onboarding_height, 48)
            SharePref.putInt(Constants.native_reshape_width, 120)
            SharePref.putInt(Constants.native_reshape_height, 48)
            SharePref.putInt(Constants.native_save_width, 120)
            SharePref.putInt(Constants.native_save_height, 48)
            SharePref.putInt(Constants.native_faceswap_width, 120)
            SharePref.putInt(Constants.native_faceswap_height, 48)
            SharePref.putInt(Constants.native_ai_resize_width, 120)
            SharePref.putInt(Constants.native_ai_resize_height, 48)
            SharePref.putInt(Constants.native_image_to_image_width, 120)
            SharePref.putInt(Constants.native_image_to_image_height, 48)
            SharePref.putInt(Constants.native_image_varition_width, 120)
            SharePref.putInt(Constants.native_image_varition_height, 48)
            SharePref.putInt(Constants.native_crop_width, 120)
            SharePref.putInt(Constants.native_crop_height, 48)
            SharePref.putInt(Constants.native_restyle_width, 120)
            SharePref.putInt(Constants.native_restyle_height, 48)
            SharePref.putInt(Constants.native_upscale_width, 120)
            SharePref.putInt(Constants.native_upscale_height, 48)
            SharePref.putInt(Constants.native_photo_editor_width, 120)
            SharePref.putInt(Constants.native_photo_editor_height, 48)
            SharePref.putInt(Constants.native_height, 48)
            SharePref.putBoolean(Constants.native_image, true)
            SharePref.putBoolean(Constants.native_video, true)
            SharePref.putBoolean(Constants.native_audio, true)
            SharePref.putBoolean(Constants.native_doc, true)
            SharePref.putBoolean(Constants.native_shredder, true)
            SharePref.putBoolean(Constants.native_shredder_images, true)
            SharePref.putBoolean(Constants.native_shredder_videos, true)
            SharePref.putBoolean(Constants.native_shredder_audios, true)
            SharePref.putBoolean(Constants.native_shredder_documents, true)
            SharePref.putBoolean(Constants.native_files_images, true)
            SharePref.putBoolean(Constants.native_files_audios, true)
            SharePref.putBoolean(Constants.native_files_videos, true)
            SharePref.putBoolean(Constants.native_files_documents, true)
            SharePref.putBoolean(Constants.native_success, true)
            SharePref.putBoolean(Constants.native_exit, true)
            SharePref.putBoolean(Constants.native_Splash, true)
            SharePref.putBoolean(Constants.native_processing, true)


            Log.d(
                TAG,
                "getRCValues: done::  ${SharePref.getBoolean(Constants.isAdmobEnable, false)}"
            )
            Log.d(
                TAG,
                "getRCValues: done::  ${SharePref.getBoolean(Constants.IsSplashOpenEnable, false)}"
            )

            SharePref.putString(Constants.AdTypeNative_language, "s")
            SharePref.putString(Constants.AdTypeNative_reshape, "s")
            SharePref.putString(Constants.AdTypeNative_faceswap, "s")
            SharePref.putString(Constants.AdTypeNative_ai_resize, "s")
            SharePref.putString(Constants.AdTypeNative_image_to_image, "s")
            SharePref.putString(Constants.AdTypeNative_restyle, "s")
            SharePref.putString(Constants.AdTypeNative_upscale, "s")
            SharePref.putString(Constants.AdTypeNative_image_varition, "s")
            SharePref.putString(Constants.AdTypeNative_crop, "s")
            SharePref.putString(Constants.AdTypeNative_photo_editor, "s")
            SharePref.putString(Constants.AdTypeNative_save, "s")
            SharePref.putString(Constants.AdTypeNative_apply, "s")
            SharePref.putString(Constants.AdTypeNative_onboarding, "s")
            SharePref.putString(Constants.AdTypeNative_home, "s")
            SharePref.putString(Constants.AdTypeNative_image, "s")
            SharePref.putString(Constants.AdTypeNative_video, "s")
            SharePref.putString(Constants.AdTypeNative_audio, "s")
            SharePref.putString(Constants.AdTypeNative_doc, "s")
            SharePref.putString(Constants.AdTypeNative_shredder, "s")
            SharePref.putString(Constants.AdTypeNative_shredder_images, "s")
            SharePref.putString(Constants.AdTypeNative_shredder_videos, "s")
            SharePref.putString(Constants.AdTypeNative_shredder_audios, "s")
            SharePref.putString(Constants.AdTypeNative_shredder_documents, "s")
            SharePref.putString(Constants.AdTypeNative_files_images, "s")
            SharePref.putString(Constants.AdTypeNative_files_audios, "s")
            SharePref.putString(Constants.AdTypeNative_files_videos, "s")
            SharePref.putString(Constants.AdTypeNative_files_documents, "s")
            SharePref.putString(Constants.AdTypeNative_success, "s")
            SharePref.putString(Constants.AdTypeNative_exit, "s")
            SharePref.putString(Constants.AdTypeNative_Splash, "s")
            SharePref.putString(Constants.AdTypeNative_processing, "s")
            SharePref.putString(Constants.CtaTypeNative_language, "b")
            SharePref.putString(Constants.CtaTypeNative_reshape, "b")
            SharePref.putString(Constants.CtaTypeNative_faceswap, "b")
            SharePref.putString(Constants.CtaTypeNative_ai_resize, "b")
            SharePref.putString(Constants.CtaTypeNative_image_to_image, "b")
            SharePref.putString(Constants.CtaTypeNative_restyle, "b")
            SharePref.putString(Constants.CtaTypeNative_upscale, "b")
            SharePref.putString(Constants.CtaTypeNative_image_varition, "b")
            SharePref.putString(Constants.CtaTypeNative_crop, "b")
            SharePref.putString(Constants.CtaTypeNative_photo_editor, "b")
            SharePref.putString(Constants.CtaTypeNative_save, "b")
            SharePref.putString(Constants.CtaTypeNative_apply, "b")
            SharePref.putString(Constants.CtaTypeNative_onboarding, "b")
            SharePref.putString(Constants.CtaTypeNative_home, "b")
            SharePref.putString(Constants.CtaTypeNative_image, "b")
            SharePref.putString(Constants.CtaTypeNative_video, "b")
            SharePref.putString(Constants.CtaTypeNative_audio, "b")
            SharePref.putString(Constants.CtaTypeNative_doc, "b")
            SharePref.putString(Constants.CtaTypeNative_shredder, "b")
            SharePref.putString(Constants.CtaTypeNative_shredder_images, "b")
            SharePref.putString(Constants.CtaTypeNative_shredder_videos, "b")
            SharePref.putString(Constants.CtaTypeNative_shredder_audios, "b")
            SharePref.putString(Constants.CtaTypeNative_shredder_documents, "b")
            SharePref.putString(Constants.CtaTypeNative_files_images, "b")
            SharePref.putString(Constants.CtaTypeNative_files_audios, "b")
            SharePref.putString(Constants.CtaTypeNative_files_videos, "b")
            SharePref.putString(Constants.CtaTypeNative_files_documents, "b")
            SharePref.putString(Constants.CtaTypeNative_success, "b")
            SharePref.putString(Constants.CtaTypeNative_exit, "b")
            SharePref.putString(Constants.CtaTypeNative_Splash, "b")
            SharePref.putString(Constants.CtaTypeNative_processing, "b")

            // ── native_saved ───────────────────────────────────────────
            SharePref.putBoolean(Constants.native_saved, true)
            SharePref.putBoolean(Constants.rectangle_banner_saved, true)
            SharePref.putString(
                Constants.native_saved_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(
                Constants.rectangle_banner_saved_key,
                "ca-app-pub-3940256099942544/2247696110"
            )
            SharePref.putString(Constants.native_saved_cta_color, "#000000")
            SharePref.putString(Constants.native_saved_cta_text_color, "#ffffff")
            SharePref.putInt(Constants.native_saved_width, 120)
            SharePref.putInt(Constants.native_saved_height, 48)
            SharePref.putString(Constants.AdTypeNative_saved, "s")
            SharePref.putString(Constants.CtaTypeNative_saved, "b")


        }

        private fun fetchRcKeys(remoteConfig: FirebaseRemoteConfig) {
            val fetchedApiKey = remoteConfig.getString(Constants.api_key)
            val fetchedNewVersionKey = remoteConfig.getString(Constants.new_version_key)
            logFetchedApiKeys(
                source = "RELEASE_FETCH",
                apiKey = fetchedApiKey,
                newVersionKey = fetchedNewVersionKey
            )


            Log.e("RC_FETCH", "========== FIREBASE RC RAW VALUES ==========")

            // --- FLAGS ---
            Log.e("RC_FETCH", "--- ENABLE FLAGS ---")
            Log.e(
                "RC_FETCH",
                "isAdmobEnable            : ${remoteConfig.getBoolean(Constants.isAdmobEnable)}"
            )
            Log.e(
                "RC_FETCH",
                "IsOpenAdEnable           : ${remoteConfig.getBoolean(Constants.IsOpenAdEnable)}"
            )
            Log.e(
                "RC_FETCH",
                "IsSplashInterEnable      : ${remoteConfig.getBoolean(Constants.IsSplashInterEnable)}"
            )
            Log.e(
                "RC_FETCH",
                "IsSplashOpenEnable       : ${remoteConfig.getBoolean(Constants.IsSplashOpenEnable)}"
            )
            Log.e(
                "RC_FETCH",
                "Ishome_inter_ad_key      : ${remoteConfig.getBoolean(Constants.Ishome_inter_ad_key)}"
            )
            Log.e(
                "RC_FETCH",
                "IsLang_Interstitial_key  : ${remoteConfig.getBoolean(Constants.IsLang_Interstitial_key)}"
            )
            Log.e(
                "RC_FETCH",
                "IsRewardedAdEnable       : ${remoteConfig.getBoolean(Constants.IsRewardedAdEnable)}"
            )
            Log.e(
                "RC_FETCH",
                "Is_Purchase_Inter_key    : ${remoteConfig.getBoolean(Constants.Is_Purchase_Interstitial_key)}"
            )

            // --- NATIVE FLAGS ---
            Log.e("RC_FETCH", "--- NATIVE FLAGS ---")
            Log.e(
                "RC_FETCH",
                "native_language          : ${remoteConfig.getBoolean(Constants.native_language)}"
            )
            Log.e(
                "RC_FETCH",
                "native_reshape           : ${remoteConfig.getBoolean(Constants.native_reshape)}"
            )
            Log.e(
                "RC_FETCH",
                "native_faceswap          : ${remoteConfig.getBoolean(Constants.native_faceswap)}"
            )
            Log.e(
                "RC_FETCH",
                "native_ai_resize         : ${remoteConfig.getBoolean(Constants.native_ai_resize)}"
            )
            Log.e(
                "RC_FETCH",
                "native_image_to_image    : ${remoteConfig.getBoolean(Constants.native_image_to_image)}"
            )
            Log.e(
                "RC_FETCH",
                "native_restyle           : ${remoteConfig.getBoolean(Constants.native_restyle)}"
            )
            Log.e(
                "RC_FETCH",
                "native_upscale           : ${remoteConfig.getBoolean(Constants.native_upscale)}"
            )
            Log.e(
                "RC_FETCH",
                "native_image_varition    : ${remoteConfig.getBoolean(Constants.native_image_varition)}"
            )
            Log.e(
                "RC_FETCH",
                "native_crop              : ${remoteConfig.getBoolean(Constants.native_crop)}"
            )
            Log.e(
                "RC_FETCH",
                "native_photo_editor      : ${remoteConfig.getBoolean(Constants.native_photo_editor)}"
            )
            Log.e(
                "RC_FETCH",
                "native_save              : ${remoteConfig.getBoolean(Constants.native_save)}"
            )
            Log.e(
                "RC_FETCH",
                "native_apply             : ${remoteConfig.getBoolean(Constants.native_apply)}"
            )
            Log.e(
                "RC_FETCH",
                "native_home              : ${remoteConfig.getBoolean(Constants.native_home)}"
            )
            Log.e(
                "RC_FETCH",
                "native_onboarding        : ${remoteConfig.getBoolean(Constants.native_onboarding)}"
            )

            // --- BANNER FLAGS ---
            Log.e("RC_FETCH", "--- BANNER FLAGS ---")
            Log.e(
                "RC_FETCH",
                "banner_main              : ${remoteConfig.getBoolean(Constants.banner_main)}"
            )
            Log.e(
                "RC_FETCH",
                "banner_select_pose       : ${remoteConfig.getBoolean(Constants.banner_select_pose)}"
            )
            Log.e(
                "RC_FETCH",
                "banner_bg_remover        : ${remoteConfig.getBoolean(Constants.banner_bg_remover)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_reshape : ${remoteConfig.getBoolean(Constants.rectangle_banner_reshape)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_faceswap: ${remoteConfig.getBoolean(Constants.rectangle_banner_faceswape)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_ai_size : ${remoteConfig.getBoolean(Constants.rectangle_banner_ai_resize)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_crop    : ${remoteConfig.getBoolean(Constants.rectangle_banner_crop)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_img2img : ${remoteConfig.getBoolean(Constants.rectangle_banner_image_to_image)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_restyle : ${remoteConfig.getBoolean(Constants.rectangle_banner_restyle)}"
            )
            Log.e(
                "RC_FETCH",
                "rectangle_banner_upscale : ${remoteConfig.getBoolean(Constants.rectangle_banner_upscale)}"
            )
            Log.e(
                "RC_FETCH",
                "banner_face_style        : ${remoteConfig.getBoolean(Constants.banner_face_style)}"
            )
            Log.e(
                "RC_FETCH",
                "banner_text_to_image     : ${remoteConfig.getBoolean(Constants.banner_text_to_image)}"
            )

            // --- AD UNIT IDs ---
            Log.e("RC_FETCH", "--- AD UNIT IDs ---")
            Log.e(
                "RC_FETCH",
                "AdmobNativeId            : '${remoteConfig.getString(Constants.AdmobNativeId)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobNativeIdHigh        : '${remoteConfig.getString(Constants.AdmobNativeIdHigh)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobInterId             : '${remoteConfig.getString(Constants.AdmobInterId)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobInterIdHigh         : '${remoteConfig.getString(Constants.AdmobInterIdHigh)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobInterstitialId      : '${remoteConfig.getString(Constants.AdmobInterstitialId)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobBannerId            : '${remoteConfig.getString(Constants.AdmobBannerId)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobBannerIdHigh        : '${remoteConfig.getString(Constants.AdmobBannerIdHigh)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobOpenAdId            : '${remoteConfig.getString(Constants.AdmobOpenAdId)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobOpenAdIdHigh        : '${remoteConfig.getString(Constants.AdmobOpenAdIdHigh)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobSplashOpenId        : '${remoteConfig.getString(Constants.AdmobSplashOpenId)}'"
            )
            Log.e(
                "RC_FETCH",
                "AdmobSplashInterId       : '${remoteConfig.getString(Constants.AdmobSplashInterId)}'"
            )
            Log.e(
                "RC_FETCH",
                "rewardedAdId             : '${remoteConfig.getString(Constants.rewardedAdId)}'"
            )
            Log.e(
                "RC_FETCH",
                "Lang_Interstitial_key    : '${remoteConfig.getString(Constants.Lang_Interstitial_key)}'"
            )
            Log.e(
                "RC_FETCH",
                "Purchase_Inter_key       : '${remoteConfig.getString(Constants.Purchase_Interstitial_key)}'"
            )
            Log.e(
                "RC_FETCH",
                "api_key                  : '${remoteConfig.getString(Constants.api_key)}'"
            )

            // --- RESHAPE SPECIFIC ---
            Log.e("RC_FETCH", "--- RESHAPE SPECIFIC ---")
            Log.e(
                "RC_FETCH",
                "native_reshape_key       : '${remoteConfig.getString(Constants.native_reshape_key)}'"
            )
            Log.e(
                "RC_FETCH",
                "rect_banner_reshape_key  : '${remoteConfig.getString(Constants.rectangle_banner_reshape_key)}'"
            )
            Log.e(
                "RC_FETCH",
                "native_reshape_cta_color : '${remoteConfig.getString(Constants.native_reshape_cta_color)}'"
            )
            Log.e(
                "RC_FETCH",
                "native_reshape_cta_txtclr: '${remoteConfig.getString(Constants.native_reshape_cta_text_color)}'"
            )
            Log.e(
                "RC_FETCH",
                "native_reshape_width     : ${remoteConfig.getLong(Constants.native_reshape_width)}"
            )
            Log.e(
                "RC_FETCH",
                "native_reshape_height    : ${remoteConfig.getLong(Constants.native_reshape_height)}"
            )
            Log.e(
                "RC_FETCH",
                "AdTypeNative_reshape     : '${remoteConfig.getString(Constants.AdTypeNative_reshape)}'"
            )
            Log.e(
                "RC_FETCH",
                "CtaTypeNative_reshape    : '${remoteConfig.getString(Constants.CtaTypeNative_reshape)}'"
            )

            // --- EMPTY ID WARNINGS ---
            Log.e("RC_FETCH", "--- ❌ EMPTY ID WARNINGS ---")
            if (remoteConfig.getString(Constants.AdmobNativeId).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ AdmobNativeId is EMPTY in RC!"
            )
            if (remoteConfig.getString(Constants.AdmobInterId).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ AdmobInterId is EMPTY in RC!"
            )
            if (remoteConfig.getString(Constants.AdmobBannerId).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ AdmobBannerId is EMPTY in RC!"
            )
            if (remoteConfig.getString(Constants.AdmobOpenAdId).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ AdmobOpenAdId is EMPTY in RC!"
            )
            if (remoteConfig.getString(Constants.rewardedAdId).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ rewardedAdId is EMPTY in RC!"
            )
            if (remoteConfig.getString(Constants.native_reshape_key).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ native_reshape_key is EMPTY in RC!"
            )
            if (remoteConfig.getString(Constants.rectangle_banner_reshape_key)
                    .isEmpty()
            ) Log.e("RC_FETCH", "❌ rectangle_banner_reshape_key is EMPTY in RC!")
            if (remoteConfig.getString(Constants.api_key).isEmpty()) Log.e(
                "RC_FETCH",
                "❌ api_key is EMPTY in RC!"
            )

            // --- SHAREPREF VERIFY (what actually got saved) ---
            Log.e("RC_FETCH", "--- ✅ SHAREPREF SAVED VALUES (what fragments will read) ---")
            Log.e(
                "RC_FETCH",
                "SP native_reshape        : ${
                    SharePref.getBoolean(
                        Constants.native_reshape,
                        false
                    )
                }"
            )
            Log.e(
                "RC_FETCH",
                "SP rect_banner_reshape   : ${
                    SharePref.getBoolean(
                        Constants.rectangle_banner_reshape,
                        false
                    )
                }"
            )
            Log.e(
                "RC_FETCH",
                "SP native_reshape_key    : '${
                    SharePref.getString(
                        Constants.native_reshape_key,
                        ""
                    )
                }'"
            )
            Log.e(
                "RC_FETCH",
                "SP rect_banner_reshp_key : '${
                    SharePref.getString(
                        Constants.rectangle_banner_reshape_key,
                        ""
                    )
                }'"
            )
            Log.e(
                "RC_FETCH",
                "SP AdmobNativeId         : '${SharePref.getString(Constants.AdmobNativeId, "")}'"
            )
            Log.e(
                "RC_FETCH",
                "SP AdmobBannerId         : '${SharePref.getString(Constants.AdmobBannerId, "")}'"
            )
            Log.e(
                "RC_FETCH",
                "SP isAdmobEnable         : ${SharePref.getBoolean(Constants.isAdmobEnable, false)}"
            )
            Log.e("RC_FETCH", "============================================")

            SharePref.putBoolean(
                Constants.isAdmobEnable,
                remoteConfig.getBoolean(Constants.isAdmobEnable)
            )
            SharePref.putBoolean(
                Constants.IsOpenAdEnable,
                remoteConfig.getBoolean(Constants.IsOpenAdEnable)
            )
            SharePref.putBoolean(
                Constants.IsSplashInterEnable,
                remoteConfig.getBoolean(Constants.IsSplashInterEnable)
            )
            SharePref.putBoolean(
                Constants.IsSplashOpenEnable,
                remoteConfig.getBoolean(Constants.IsSplashOpenEnable)
            )

            // banner facebook ids
            SharePref.putBoolean(
                Constants.banner_main,
                remoteConfig.getBoolean(Constants.banner_main)
            )
            SharePref.putBoolean(
                Constants.isCollapsible,
                remoteConfig.getBoolean(Constants.isCollapsible)
            )
            SharePref.putBoolean(
                Constants.isBannerCollapsibleDown,
                remoteConfig.getBoolean(Constants.isBannerCollapsibleDown)
            )

            // native admob ids
            SharePref.putBoolean(
                Constants.native_language,
                remoteConfig.getBoolean(Constants.native_language)
            )
            SharePref.putBoolean(
                Constants.native_reshape,
                remoteConfig.getBoolean(Constants.native_reshape)
            )
            SharePref.putBoolean(
                Constants.banner_select_pose,
                remoteConfig.getBoolean(Constants.banner_select_pose)
            )
            SharePref.putBoolean(
                Constants.banner_bg_remover,
                remoteConfig.getBoolean(Constants.banner_bg_remover)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_ai_resize,
                remoteConfig.getBoolean(Constants.rectangle_banner_ai_resize)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_crop,
                remoteConfig.getBoolean(Constants.rectangle_banner_crop)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_image_to_image,
                remoteConfig.getBoolean(Constants.rectangle_banner_image_to_image)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_image_varition,
                remoteConfig.getBoolean(Constants.rectangle_banner_image_varition)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_photo_editor,
                remoteConfig.getBoolean(Constants.rectangle_banner_photo_editor)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_reshape,
                remoteConfig.getBoolean(Constants.rectangle_banner_reshape)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_restyle,
                remoteConfig.getBoolean(Constants.rectangle_banner_restyle)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_upscale,
                remoteConfig.getBoolean(Constants.rectangle_banner_upscale)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_faceswape,
                remoteConfig.getBoolean(Constants.rectangle_banner_faceswape)
            )
            SharePref.putBoolean(
                Constants.banner_face_style,
                remoteConfig.getBoolean(Constants.banner_face_style)
            )
            SharePref.putBoolean(
                Constants.banner_text_to_image,
                remoteConfig.getBoolean(Constants.banner_text_to_image)
            )
            SharePref.putBoolean(
                Constants.native_faceswap,
                remoteConfig.getBoolean(Constants.native_faceswap)
            )
            SharePref.putBoolean(
                Constants.native_ai_resize,
                remoteConfig.getBoolean(Constants.native_ai_resize)
            )
            SharePref.putBoolean(
                Constants.native_image_to_image,
                remoteConfig.getBoolean(Constants.native_image_to_image)
            )
            SharePref.putBoolean(
                Constants.native_restyle,
                remoteConfig.getBoolean(Constants.native_restyle)
            )
            SharePref.putBoolean(
                Constants.native_upscale,
                remoteConfig.getBoolean(Constants.native_upscale)
            )
            SharePref.putBoolean(
                Constants.native_image_varition,
                remoteConfig.getBoolean(Constants.native_image_varition)
            )
            SharePref.putBoolean(
                Constants.native_crop,
                remoteConfig.getBoolean(Constants.native_crop)
            )
            SharePref.putBoolean(
                Constants.native_photo_editor,
                remoteConfig.getBoolean(Constants.native_photo_editor)
            )



            SharePref.putBoolean(
                Constants.native_save,
                remoteConfig.getBoolean(Constants.native_save)
            )

            SharePref.putBoolean(
                Constants.native_apply,
                remoteConfig.getBoolean(Constants.native_apply)
            )
            SharePref.putBoolean(
                Constants.native_apply,
                remoteConfig.getBoolean(Constants.native_apply)
            )
            SharePref.putString(
                Constants.native_language_key,
                remoteConfig.getString(Constants.native_language_key)
            )
            SharePref.putString(
                Constants.native_onboarding_key,
                remoteConfig.getString(Constants.native_onboarding_key)
            )
            SharePref.putString(
                Constants.native_language_cta_color,
                remoteConfig.getString(Constants.native_language_cta_color)
            )
            SharePref.putString(
                Constants.native_language_cta_text_color,
                remoteConfig.getString(Constants.native_language_cta_text_color)
            )


            SharePref.putString(
                Constants.native_faceswap_key,
                remoteConfig.getString(Constants.native_faceswap_key)
            )
            SharePref.putString(
                Constants.native_ai_resize_key,
                remoteConfig.getString(Constants.native_ai_resize_key)
            )
            SharePref.putString(
                Constants.native_image_to_image_key,
                remoteConfig.getString(Constants.native_image_to_image_key)
            )
            SharePref.putString(
                Constants.native_restyle_key,
                remoteConfig.getString(Constants.native_restyle_key)
            )
            SharePref.putString(
                Constants.native_upscale_key,
                remoteConfig.getString(Constants.native_upscale_key)
            )
            SharePref.putString(
                Constants.native_image_varition_key,
                remoteConfig.getString(Constants.native_image_varition_key)
            )
            SharePref.putString(
                Constants.native_crop_key,
                remoteConfig.getString(Constants.native_crop_key)
            )
            SharePref.putString(
                Constants.native_photo_editor_key,
                remoteConfig.getString(Constants.native_photo_editor_key)
            )
            SharePref.putString(
                Constants.native_save_key,
                remoteConfig.getString(Constants.native_save_key)
            )
            SharePref.putString(
                Constants.api_key,
                remoteConfig.getString(Constants.api_key)
            )

            SharePref.putString(
                Constants.new_version_key,
                remoteConfig.getString(Constants.new_version_key)  // "api_key"
            )

            SharePref.putString(
                Constants.native_apply_key,
                remoteConfig.getString(Constants.native_apply_key)
            )
            SharePref.putString(
                Constants.native_onboarding_ful_key,
                remoteConfig.getString(Constants.native_onboarding_ful_key)
            )
            SharePref.putBoolean(
                Constants.native_onboarding,
                remoteConfig.getBoolean(Constants.native_onboarding)
            )
            SharePref.putBoolean(
                Constants.is_native_onboarding_full,
                remoteConfig.getBoolean(Constants.is_native_onboarding_full)
            )
            SharePref.putBoolean(
                Constants.native_home,
                remoteConfig.getBoolean(Constants.native_home)
            )
            SharePref.putInt(
                Constants.native_width,
                remoteConfig.getLong(Constants.native_width).toInt()
            )
            SharePref.putInt(
                Constants.native_height,
                remoteConfig.getLong(Constants.native_height).toInt()
            )

            SharePref.putInt(
                Constants.native_height,
                remoteConfig.getLong(Constants.native_height).toInt()
            )

            SharePref.putInt(
                Constants.native_language_width,
                remoteConfig.getLong(Constants.native_language_width).toInt()
            )
            SharePref.putInt(
                Constants.native_language_height,
                remoteConfig.getLong(Constants.native_language_height).toInt()
            )

            /* new keys for native*/
            SharePref.putString(
                Constants.native_onboarding_cta_color,
                remoteConfig.getString(Constants.native_onboarding_cta_color)
            )
            SharePref.putString(
                Constants.native_onboarding_cta_text_color,
                remoteConfig.getString(Constants.native_onboarding_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_onboarding_width,
                remoteConfig.getLong(Constants.native_onboarding_width).toInt()
            )
            SharePref.putInt(
                Constants.native_onboarding_height,
                remoteConfig.getLong(Constants.native_onboarding_height).toInt()
            )


            SharePref.putString(
                Constants.native_reshape_cta_color,
                remoteConfig.getString(Constants.native_reshape_cta_color)
            )
            SharePref.putString(
                Constants.native_reshape_cta_text_color,
                remoteConfig.getString(Constants.native_reshape_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_reshape_width,
                remoteConfig.getLong(Constants.native_reshape_width).toInt()
            )
            SharePref.putInt(
                Constants.native_reshape_height,
                remoteConfig.getLong(Constants.native_reshape_height).toInt()
            )



            SharePref.putString(
                Constants.native_save_cta_color,
                remoteConfig.getString(Constants.native_save_cta_color)
            )
            SharePref.putString(
                Constants.native_save_cta_text_color,
                remoteConfig.getString(Constants.native_save_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_save_width,
                remoteConfig.getLong(Constants.native_save_width).toInt()
            )
            SharePref.putInt(
                Constants.native_save_height,
                remoteConfig.getLong(Constants.native_save_height).toInt()
            )


            SharePref.putString(
                Constants.native_faceswap_cta_color,
                remoteConfig.getString(Constants.native_faceswap_cta_color)
            )
            SharePref.putString(
                Constants.native_faceswap_cta_text_color,
                remoteConfig.getString(Constants.native_faceswap_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_faceswap_width,
                remoteConfig.getLong(Constants.native_faceswap_width).toInt()
            )
            SharePref.putInt(
                Constants.native_faceswap_height,
                remoteConfig.getLong(Constants.native_faceswap_height).toInt()
            )



            SharePref.putString(
                Constants.native_ai_resize_cta_color,
                remoteConfig.getString(Constants.native_ai_resize_cta_color)
            )
            SharePref.putString(
                Constants.native_ai_resize_cta_text_color,
                remoteConfig.getString(Constants.native_ai_resize_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_ai_resize_width,
                remoteConfig.getLong(Constants.native_ai_resize_width).toInt()
            )
            SharePref.putInt(
                Constants.native_ai_resize_height,
                remoteConfig.getLong(Constants.native_ai_resize_height).toInt()
            )


            SharePref.putString(
                Constants.native_image_to_image_cta_color,
                remoteConfig.getString(Constants.native_image_to_image_cta_color)
            )
            SharePref.putString(
                Constants.native_image_to_image_cta_text_color,
                remoteConfig.getString(Constants.native_image_to_image_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_image_to_image_width,
                remoteConfig.getLong(Constants.native_image_to_image_width).toInt()
            )
            SharePref.putInt(
                Constants.native_image_to_image_height,
                remoteConfig.getLong(Constants.native_image_to_image_height).toInt()
            )



            SharePref.putString(
                Constants.native_image_varition_cta_color,
                remoteConfig.getString(Constants.native_image_varition_cta_color)
            )
            SharePref.putString(
                Constants.native_image_varition_cta_text_color,
                remoteConfig.getString(Constants.native_image_varition_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_image_varition_width,
                remoteConfig.getLong(Constants.native_image_varition_width).toInt()
            )
            SharePref.putInt(
                Constants.native_image_varition_height,
                remoteConfig.getLong(Constants.native_image_varition_height).toInt()
            )




            SharePref.putString(
                Constants.native_crop_cta_color,
                remoteConfig.getString(Constants.native_crop_cta_color)
            )
            SharePref.putString(
                Constants.native_crop_cta_text_color,
                remoteConfig.getString(Constants.native_crop_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_crop_width,
                remoteConfig.getLong(Constants.native_crop_width).toInt()
            )
            SharePref.putInt(
                Constants.native_crop_height,
                remoteConfig.getLong(Constants.native_crop_height).toInt()
            )



            SharePref.putString(
                Constants.native_restyle_cta_color,
                remoteConfig.getString(Constants.native_restyle_cta_color)
            )
            SharePref.putString(
                Constants.native_restyle_cta_text_color,
                remoteConfig.getString(Constants.native_restyle_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_restyle_width,
                remoteConfig.getLong(Constants.native_restyle_width).toInt()
            )
            SharePref.putInt(
                Constants.native_restyle_height,
                remoteConfig.getLong(Constants.native_restyle_height).toInt()
            )



            SharePref.putString(
                Constants.native_upscale_cta_color,
                remoteConfig.getString(Constants.native_upscale_cta_color)
            )
            SharePref.putString(
                Constants.native_upscale_cta_text_color,
                remoteConfig.getString(Constants.native_upscale_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_upscale_width,
                remoteConfig.getLong(Constants.native_upscale_width).toInt()
            )
            SharePref.putInt(
                Constants.native_upscale_height,
                remoteConfig.getLong(Constants.native_upscale_height).toInt()
            )


            SharePref.putString(
                Constants.native_photo_editor_cta_color,
                remoteConfig.getString(Constants.native_photo_editor_cta_color)
            )
            SharePref.putString(
                Constants.native_photo_editor_cta_text_color,
                remoteConfig.getString(Constants.native_photo_editor_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_photo_editor_width,
                remoteConfig.getLong(Constants.native_photo_editor_width).toInt()
            )
            SharePref.putInt(
                Constants.native_photo_editor_height,
                remoteConfig.getLong(Constants.native_photo_editor_height).toInt()
            )
            /*end*/

            SharePref.putString(
                Constants.native_reshape_key,
                remoteConfig.getString(Constants.native_reshape_key)
            )
            SharePref.putString(
                Constants.banner_select_pose_key,
                remoteConfig.getString(Constants.banner_select_pose_key)
            )
            SharePref.putString(
                Constants.banner_bg_remover_key,
                remoteConfig.getString(Constants.banner_bg_remover_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_ai_size_key,
                remoteConfig.getString(Constants.rectangle_banner_ai_size_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_crop_key,
                remoteConfig.getString(Constants.rectangle_banner_crop_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_image_to_image_key,
                remoteConfig.getString(Constants.rectangle_banner_image_to_image_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_image_varition_key,
                remoteConfig.getString(Constants.rectangle_banner_image_varition_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_photo_editor_key,
                remoteConfig.getString(Constants.rectangle_banner_photo_editor_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_reshape_key,
                remoteConfig.getString(Constants.rectangle_banner_reshape_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_restyle_key,
                remoteConfig.getString(Constants.rectangle_banner_restyle_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_upscale_key,
                remoteConfig.getString(Constants.rectangle_banner_upscale_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_faceswap_key,
                remoteConfig.getString(Constants.rectangle_banner_faceswap_key)
            )
            SharePref.putString(
                Constants.banner_face_style_key,
                remoteConfig.getString(Constants.banner_face_style_key)
            )
            SharePref.putString(
                Constants.banner_text_to_image_key,
                remoteConfig.getString(Constants.banner_text_to_image_key)
            )
            SharePref.putBoolean(
                Constants.native_image,
                remoteConfig.getBoolean(Constants.native_image)
            )
            SharePref.putBoolean(
                Constants.native_video,
                remoteConfig.getBoolean(Constants.native_video)
            )
            SharePref.putBoolean(
                Constants.native_audio,
                remoteConfig.getBoolean(Constants.native_audio)
            )
            SharePref.putBoolean(
                Constants.native_doc,
                remoteConfig.getBoolean(Constants.native_doc)
            )
            SharePref.putBoolean(
                Constants.native_shredder,
                remoteConfig.getBoolean(Constants.native_shredder)
            )
            SharePref.putBoolean(
                Constants.native_shredder_images,
                remoteConfig.getBoolean(Constants.native_shredder_images)
            )
            SharePref.putBoolean(
                Constants.native_shredder_videos,
                remoteConfig.getBoolean(Constants.native_shredder_videos)
            )
            SharePref.putBoolean(
                Constants.native_shredder_audios,
                remoteConfig.getBoolean(Constants.native_shredder_audios)
            )
            SharePref.putBoolean(
                Constants.native_shredder_documents,
                remoteConfig.getBoolean(Constants.native_shredder_documents)
            )
            SharePref.putBoolean(
                Constants.native_files_images,
                remoteConfig.getBoolean(Constants.native_files_images)
            )
            SharePref.putBoolean(
                Constants.native_files_audios,
                remoteConfig.getBoolean(Constants.native_files_audios)
            )
            SharePref.putBoolean(
                Constants.native_files_videos,
                remoteConfig.getBoolean(Constants.native_files_videos)
            )
            SharePref.putBoolean(
                Constants.native_files_documents,
                remoteConfig.getBoolean(Constants.native_files_documents)
            )
            SharePref.putBoolean(
                Constants.native_success,
                remoteConfig.getBoolean(Constants.native_success)
            )
            SharePref.putBoolean(
                Constants.native_exit,
                remoteConfig.getBoolean(Constants.native_exit)
            )
            SharePref.putBoolean(
                Constants.native_Splash,
                remoteConfig.getBoolean(Constants.native_Splash)
            )
            SharePref.putBoolean(
                Constants.native_processing,
                remoteConfig.getBoolean(Constants.native_processing)
            )


            SharePref.putString(
                Constants.AdTypeNative_language,
                remoteConfig.getString(Constants.AdTypeNative_language)
            )
            SharePref.putString(
                Constants.AdTypeNative_reshape,
                remoteConfig.getString(Constants.AdTypeNative_reshape)
            )
            SharePref.putString(
                Constants.AdTypeNative_faceswap,
                remoteConfig.getString(Constants.AdTypeNative_faceswap)
            )
            SharePref.putString(
                Constants.AdTypeNative_ai_resize,
                remoteConfig.getString(Constants.AdTypeNative_ai_resize)
            )
            SharePref.putString(
                Constants.AdTypeNative_image_to_image,
                remoteConfig.getString(Constants.AdTypeNative_image_to_image)
            )
            SharePref.putString(
                Constants.AdTypeNative_restyle,
                remoteConfig.getString(Constants.AdTypeNative_restyle)
            )
            SharePref.putString(
                Constants.AdTypeNative_upscale,
                remoteConfig.getString(Constants.AdTypeNative_upscale)
            )
            SharePref.putString(
                Constants.AdTypeNative_image_varition,
                remoteConfig.getString(Constants.AdTypeNative_image_varition)
            )
            SharePref.putString(
                Constants.AdTypeNative_crop,
                remoteConfig.getString(Constants.AdTypeNative_crop)
            )
            SharePref.putString(
                Constants.AdTypeNative_photo_editor,
                remoteConfig.getString(Constants.AdTypeNative_photo_editor)
            )
            SharePref.putString(
                Constants.AdTypeNative_save,
                remoteConfig.getString(Constants.AdTypeNative_save)
            )
            SharePref.putString(
                Constants.AdTypeNative_apply,
                remoteConfig.getString(Constants.AdTypeNative_apply)
            )
            SharePref.putString(
                Constants.AdTypeNative_onboarding,
                remoteConfig.getString(Constants.AdTypeNative_onboarding)
            )
            SharePref.putString(
                Constants.AdTypeNative_home,
                remoteConfig.getString(Constants.AdTypeNative_home)
            )
            SharePref.putString(
                Constants.AdTypeNative_image,
                remoteConfig.getString(Constants.AdTypeNative_image)
            )
            SharePref.putString(
                Constants.AdTypeNative_video,
                remoteConfig.getString(Constants.AdTypeNative_video)
            )
            SharePref.putString(
                Constants.AdTypeNative_audio,
                remoteConfig.getString(Constants.AdTypeNative_audio)
            )
            SharePref.putString(
                Constants.AdTypeNative_doc,
                remoteConfig.getString(Constants.AdTypeNative_doc)
            )
            SharePref.putString(
                Constants.AdTypeNative_shredder,
                remoteConfig.getString(Constants.AdTypeNative_shredder)
            )
            SharePref.putString(
                Constants.AdTypeNative_shredder_images,
                remoteConfig.getString(Constants.AdTypeNative_shredder_images)
            )
            SharePref.putString(
                Constants.AdTypeNative_shredder_videos,
                remoteConfig.getString(Constants.AdTypeNative_shredder_videos)
            )
            SharePref.putString(
                Constants.AdTypeNative_shredder_audios,
                remoteConfig.getString(Constants.AdTypeNative_shredder_audios)
            )
            SharePref.putString(
                Constants.AdTypeNative_shredder_documents,
                remoteConfig.getString(Constants.AdTypeNative_shredder_documents)
            )
            SharePref.putString(
                Constants.AdTypeNative_files_images,
                remoteConfig.getString(Constants.AdTypeNative_files_images)
            )
            SharePref.putString(
                Constants.AdTypeNative_files_audios,
                remoteConfig.getString(Constants.AdTypeNative_files_audios)
            )
            SharePref.putString(
                Constants.AdTypeNative_files_videos,
                remoteConfig.getString(Constants.AdTypeNative_files_videos)
            )
            SharePref.putString(
                Constants.AdTypeNative_files_documents,
                remoteConfig.getString(Constants.AdTypeNative_files_documents)
            )
            SharePref.putString(
                Constants.AdTypeNative_success,
                remoteConfig.getString(Constants.AdTypeNative_success)
            )
            SharePref.putString(
                Constants.AdTypeNative_exit,
                remoteConfig.getString(Constants.AdTypeNative_exit)
            )
            SharePref.putString(
                Constants.AdTypeNative_Splash,
                remoteConfig.getString(Constants.AdTypeNative_Splash)
            )
            SharePref.putString(
                Constants.AdTypeNative_processing,
                remoteConfig.getString(Constants.AdTypeNative_processing)
            )

            SharePref.putString(
                Constants.CtaTypeNative_language,
                remoteConfig.getString(Constants.CtaTypeNative_language)
            )
            SharePref.putString(
                Constants.CtaTypeNative_reshape,
                remoteConfig.getString(Constants.CtaTypeNative_reshape)
            )
            SharePref.putString(
                Constants.CtaTypeNative_faceswap,
                remoteConfig.getString(Constants.CtaTypeNative_faceswap)
            )
            SharePref.putString(
                Constants.CtaTypeNative_ai_resize,
                remoteConfig.getString(Constants.CtaTypeNative_ai_resize)
            )
            SharePref.putString(
                Constants.CtaTypeNative_image_to_image,
                remoteConfig.getString(Constants.CtaTypeNative_image_to_image)
            )
            SharePref.putString(
                Constants.CtaTypeNative_restyle,
                remoteConfig.getString(Constants.CtaTypeNative_restyle)
            )
            SharePref.putString(
                Constants.CtaTypeNative_upscale,
                remoteConfig.getString(Constants.CtaTypeNative_upscale)
            )
            SharePref.putString(
                Constants.CtaTypeNative_image_varition,
                remoteConfig.getString(Constants.CtaTypeNative_image_varition)
            )
            SharePref.putString(
                Constants.CtaTypeNative_crop,
                remoteConfig.getString(Constants.CtaTypeNative_crop)
            )
            SharePref.putString(
                Constants.CtaTypeNative_photo_editor,
                remoteConfig.getString(Constants.CtaTypeNative_photo_editor)
            )
            SharePref.putString(
                Constants.CtaTypeNative_save,
                remoteConfig.getString(Constants.CtaTypeNative_save)
            )
            SharePref.putString(
                Constants.CtaTypeNative_apply,
                remoteConfig.getString(Constants.CtaTypeNative_apply)
            )
            SharePref.putString(
                Constants.CtaTypeNative_onboarding,
                remoteConfig.getString(Constants.CtaTypeNative_onboarding)
            )
            SharePref.putString(
                Constants.CtaTypeNative_home,
                remoteConfig.getString(Constants.CtaTypeNative_home)
            )
            SharePref.putString(
                Constants.CtaTypeNative_image,
                remoteConfig.getString(Constants.CtaTypeNative_image)
            )
            SharePref.putString(
                Constants.CtaTypeNative_video,
                remoteConfig.getString(Constants.CtaTypeNative_video)
            )
            SharePref.putString(
                Constants.CtaTypeNative_audio,
                remoteConfig.getString(Constants.CtaTypeNative_audio)
            )
            SharePref.putString(
                Constants.CtaTypeNative_doc,
                remoteConfig.getString(Constants.CtaTypeNative_doc)
            )
            SharePref.putString(
                Constants.CtaTypeNative_shredder,
                remoteConfig.getString(Constants.CtaTypeNative_shredder)
            )
            SharePref.putString(
                Constants.CtaTypeNative_shredder_images,
                remoteConfig.getString(Constants.CtaTypeNative_shredder_images)
            )
            SharePref.putString(
                Constants.CtaTypeNative_shredder_videos,
                remoteConfig.getString(Constants.CtaTypeNative_shredder_videos)
            )
            SharePref.putString(
                Constants.CtaTypeNative_shredder_audios,
                remoteConfig.getString(Constants.CtaTypeNative_shredder_audios)
            )
            SharePref.putString(
                Constants.CtaTypeNative_shredder_documents,
                remoteConfig.getString(Constants.CtaTypeNative_shredder_documents)
            )
            SharePref.putString(
                Constants.CtaTypeNative_files_images,
                remoteConfig.getString(Constants.CtaTypeNative_files_images)
            )
            SharePref.putString(
                Constants.CtaTypeNative_files_audios,
                remoteConfig.getString(Constants.CtaTypeNative_files_audios)
            )
            SharePref.putString(
                Constants.CtaTypeNative_files_videos,
                remoteConfig.getString(Constants.CtaTypeNative_files_videos)
            )
            SharePref.putString(
                Constants.CtaTypeNative_files_documents,
                remoteConfig.getString(Constants.CtaTypeNative_files_documents)
            )
            SharePref.putString(
                Constants.CtaTypeNative_success,
                remoteConfig.getString(Constants.CtaTypeNative_success)
            )
            SharePref.putString(
                Constants.CtaTypeNative_exit,
                remoteConfig.getString(Constants.CtaTypeNative_exit)
            )
            SharePref.putString(
                Constants.CtaTypeNative_Splash,
                remoteConfig.getString(Constants.CtaTypeNative_Splash)
            )
            SharePref.putString(
                Constants.CtaTypeNative_processing,
                remoteConfig.getString(Constants.CtaTypeNative_processing)
            )
            SharePref.putString(
                Constants.Lang_Interstitial_key,
                remoteConfig.getString(Constants.Lang_Interstitial_key)
            )
            SharePref.putString(
                Constants.rewardedAdId,
                remoteConfig.getString(Constants.rewardedAdId)
            )
            SharePref.putString(
                Constants.Purchase_Interstitial_key,
                remoteConfig.getString(Constants.Purchase_Interstitial_key)
            )
            SharePref.putString(
                Constants.AdmobInterstitialId,
                remoteConfig.getString(Constants.AdmobInterstitialId)
            )
            SharePref.putBoolean(
                Constants.IsLang_Interstitial_key,
                remoteConfig.getBoolean(Constants.IsLang_Interstitial_key)
            )
            SharePref.putBoolean(
                Constants.IsRewardedAdEnable,
                remoteConfig.getBoolean(Constants.IsRewardedAdEnable)
            )
            SharePref.putBoolean(
                Constants.Is_Purchase_Interstitial_key,
                remoteConfig.getBoolean(Constants.Is_Purchase_Interstitial_key)
            )
            SharePref.putBoolean(
                Constants.Ishome_inter_ad_key,
                remoteConfig.getBoolean(Constants.Ishome_inter_ad_key)
            )

            SharePref.putBoolean(
                Constants.native_saved,
                remoteConfig.getBoolean(Constants.native_saved)
            )
            SharePref.putBoolean(
                Constants.rectangle_banner_saved,
                remoteConfig.getBoolean(Constants.rectangle_banner_saved)
            )
            SharePref.putString(
                Constants.native_saved_key,
                remoteConfig.getString(Constants.native_saved_key)
            )
            SharePref.putString(
                Constants.rectangle_banner_saved_key,
                remoteConfig.getString(Constants.rectangle_banner_saved_key)
            )
            SharePref.putString(
                Constants.native_saved_cta_color,
                remoteConfig.getString(Constants.native_saved_cta_color)
            )
            SharePref.putString(
                Constants.native_saved_cta_text_color,
                remoteConfig.getString(Constants.native_saved_cta_text_color)
            )
            SharePref.putInt(
                Constants.native_saved_width,
                remoteConfig.getLong(Constants.native_saved_width).toInt()
            )
            SharePref.putInt(
                Constants.native_saved_height,
                remoteConfig.getLong(Constants.native_saved_height).toInt()
            )
            SharePref.putString(
                Constants.AdTypeNative_saved,
                remoteConfig.getString(Constants.AdTypeNative_saved)
            )
            SharePref.putString(
                Constants.CtaTypeNative_saved,
                remoteConfig.getString(Constants.CtaTypeNative_saved)
            )

            Log.d(TAG, "fetchRcKeys: ${remoteConfig.getBoolean(Constants.native_language)} ...")


            Log.d(
                TAG, "fetchRcKeys: ${remoteConfig.getBoolean(Constants.native_language)}" +
                        " ${remoteConfig.getString(Constants.AdTypeNative_language)}"
            )


        }


        fun setAdData() {
            adData.inApp = SharePref.getBoolean(Constants.inApp, false)

            adData.AdmobNativeId = SharePref.getString(Constants.AdmobNativeId, "")
            adData.AdmobNativeIdHigh = SharePref.getString(Constants.AdmobNativeIdHigh, "")
            adData.AdmobInterstitialId = SharePref.getString(Constants.AdmobInterId, "")
            adData.AdmobInterstitialIdHigh = SharePref.getString(Constants.AdmobInterIdHigh, "")
            adData.Lang_Interstitial_key = SharePref.getString(Constants.Lang_Interstitial_key, "")
            adData.rewardedAdId = SharePref.getString(Constants.rewardedAdId, "")
            adData.Purchase_Interstitial_key =
                SharePref.getString(Constants.Purchase_Interstitial_key, "")
            adData.AdmobInterstitialId = SharePref.getString(Constants.AdmobInterstitialId, "")
            adData.IsLang_Interstitial_key =
                SharePref.getBoolean(Constants.IsLang_Interstitial_key, false)
            adData.IsRewardedAdEnable = SharePref.getBoolean(Constants.IsRewardedAdEnable, false)
            adData.Is_Purchase_Interstitial_key =
                SharePref.getBoolean(Constants.Is_Purchase_Interstitial_key, false)
            adData.Ishome_inter_ad_key = SharePref.getBoolean(Constants.Ishome_inter_ad_key, false)
            adData.AdmobInterstitialId = SharePref.getString(Constants.AdmobInterstitialId, "")
            adData.AdmobBannerId = SharePref.getString(Constants.AdmobBannerId, "")
            adData.AdmobBannerIdHigh = SharePref.getString(Constants.AdmobBannerIdHigh, "")
            adData.AdmobCollapsibleBannerId =
                SharePref.getString(Constants.AdmobCollapsibleBannerId, "")
            adData.AdmobCollapsibleBannerIdHigh =
                SharePref.getString(Constants.AdmobCollapsibleBannerIdHigh, "")
            adData.OpenAdId = SharePref.getString(Constants.AdmobOpenAdId, "")
            adData.OpenAdIdHigh = SharePref.getString(Constants.AdmobOpenAdIdHigh, "")
            adData.SplashInterId = SharePref.getString(Constants.AdmobSplashInterId, "")
            adData.SplashInterIdHigh = SharePref.getString(Constants.AdmobSplashInterIdHigh, "")
            adData.SplashOpenAdId = SharePref.getString(Constants.AdmobSplashOpenId, "")
            adData.SplashOpenAdIdHigh = SharePref.getString(Constants.AdmobSplashOpenIdHigh, "")
            adData.AdmobSplashNativeId = SharePref.getString(Constants.AdmobSplashNativeId, "")
            adData.AdmobCtaTextColor = SharePref.getString(Constants.AdmobCtaTextColor, "#ffffff")
            adData.AdmobCtaColor = SharePref.getString(Constants.AdmobCtaColor, "#000000")
            adData.native_width = SharePref.getInt(Constants.native_width, 120)
            adData.native_height = SharePref.getInt(Constants.native_height, 48)
            adData.CappingCounter =
                if (SharePref.getString(Constants.CappingCounter, "5").isEmpty()) 1
                else SharePref.getString(Constants.CappingCounter, "5").toInt()
            adData.isSplashInterEnable = SharePref.getBoolean(Constants.IsSplashInterEnable, false)
            adData.isSplashOpenEnable = SharePref.getBoolean(Constants.IsSplashOpenEnable, false)
            adData.isAdmobEnabled = SharePref.getBoolean(Constants.isAdmobEnable, true)
            adData.isOpenAdEnabled = SharePref.getBoolean(Constants.IsOpenAdEnable, true)

            // ← DEBUG LOGS
            Log.e("AD_DEBUG", "========== AD DATA ==========")
            Log.e("AD_DEBUG", "isAdmobEnabled     : ${adData.isAdmobEnabled}")
            Log.e("AD_DEBUG", "isOpenAdEnabled    : ${adData.isOpenAdEnabled}")
            Log.e("AD_DEBUG", "IsRewardedAdEnable : ${adData.IsRewardedAdEnable}")
            Log.e("AD_DEBUG", "isSplashInterEnable: ${adData.isSplashInterEnable}")
            Log.e("AD_DEBUG", "isSplashOpenEnable : ${adData.isSplashOpenEnable}")
            Log.e("AD_DEBUG", "Ishome_inter_ad_key: ${adData.Ishome_inter_ad_key}")
            Log.e("AD_DEBUG", "-------- IDs --------")
            Log.e("AD_DEBUG", "AdmobNativeId      : '${adData.AdmobNativeId}'")
            Log.e("AD_DEBUG", "AdmobNativeIdHigh  : '${adData.AdmobNativeIdHigh}'")
            Log.e("AD_DEBUG", "AdmobInterstitialId: '${adData.AdmobInterstitialId}'")
            Log.e("AD_DEBUG", "AdmobBannerId      : '${adData.AdmobBannerId}'")
            Log.e("AD_DEBUG", "AdmobBannerIdHigh  : '${adData.AdmobBannerIdHigh}'")
            Log.e("AD_DEBUG", "OpenAdId           : '${adData.OpenAdId}'")
            Log.e("AD_DEBUG", "OpenAdIdHigh       : '${adData.OpenAdIdHigh}'")
            Log.e("AD_DEBUG", "SplashInterId      : '${adData.SplashInterId}'")
            Log.e("AD_DEBUG", "SplashOpenAdId     : '${adData.SplashOpenAdId}'")
            Log.e("AD_DEBUG", "rewardedAdId       : '${adData.rewardedAdId}'")
            Log.e("AD_DEBUG", "-------- EMPTY CHECK --------")
            if (adData.AdmobNativeId.isEmpty()) Log.e("AD_DEBUG", "❌ AdmobNativeId is EMPTY")
            if (adData.AdmobInterstitialId.isEmpty()) Log.e(
                "AD_DEBUG",
                "❌ AdmobInterstitialId is EMPTY"
            )
            if (adData.AdmobBannerId.isEmpty()) Log.e("AD_DEBUG", "❌ AdmobBannerId is EMPTY")
            if (adData.OpenAdId.isEmpty()) Log.e("AD_DEBUG", "❌ OpenAdId is EMPTY")
            if (adData.rewardedAdId.isEmpty()) Log.e("AD_DEBUG", "❌ rewardedAdId is EMPTY")
            if (adData.SplashInterId.isEmpty()) Log.e("AD_DEBUG", "❌ SplashInterId is EMPTY")
            if (adData.SplashOpenAdId.isEmpty()) Log.e("AD_DEBUG", "❌ SplashOpenAdId is EMPTY")
            Log.e("AD_DEBUG", "=============================")
        }
    }
}
