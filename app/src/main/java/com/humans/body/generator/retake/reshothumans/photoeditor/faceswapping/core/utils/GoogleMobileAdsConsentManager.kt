package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import kotlin.also

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {

  private val consentInformation: ConsentInformation =
    UserMessagingPlatform.getConsentInformation(context)

  /** Interface definition for a callback to be invoked when consent gathering is complete. */
  fun interface OnConsentGatheringCompleteListener {
    fun consentGatheringComplete(error: FormError?)
  }

  /** Helper variable to determine if the app can request ads. */
  val canRequestAds: Boolean
    get() = consentInformation.canRequestAds()

  val isConsentAvailable: Boolean
    get() = consentInformation.isConsentFormAvailable


  /** Helper variable to determine if the privacy options form is required. */
  val isPrivacyOptionsRequired: Boolean
    get() =
      consentInformation.privacyOptionsRequirementStatus ==
              ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

  /**
   * Helper method to call the UMP SDK methods to request consent information and load/show a
   * consent form if necessary.
   */
  fun gatherConsent(
    activity: Activity,
    onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
  ) {

    val params = if (BuildConfig.DEBUG) {
      // For testing purposes only — force EEA geography so the consent form appears in debug builds
      val debugSettings = ConsentDebugSettings.Builder(activity)
        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
        .build()
      ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
    } else {
      ConsentRequestParameters.Builder().build()
    }

    // Requesting an update to consent information should be called on every app launch.
    consentInformation.requestConsentInfoUpdate(
      activity,
      params,
      {
//                if (consentInformation.isConsentFormAvailable) {
        Log.e("TESTTAG", "AVAILABLE")
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
          // Consent has been gathered.
          onConsentGatheringCompleteListener.consentGatheringComplete(formError)
          Log.e("TESTTAG", "SUCCESS ${formError?.message}")
        }
//                } else {
//                    Log.e("TESTTAG", "NOT AVAILABLE")
//                }

      },
      { requestConsentError ->
        onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
        Log.e("TESTTAG", "FAILURE ${requestConsentError.message}")
      }
    )

//    consentInformation.reset()
  }

  /** Helper method to call the UMP SDK method to show the privacy options form. */
  fun showPrivacyOptionsForm(
    activity: Activity,
    onConsentFormDismissedListener: OnConsentFormDismissedListener
  ) {
    UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
  }

  companion object {
    @Volatile
    private var instance: GoogleMobileAdsConsentManager? = null

    fun getInstance(context: Context) =
      instance
        ?: synchronized(this) {
            instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
        }
  }
}
