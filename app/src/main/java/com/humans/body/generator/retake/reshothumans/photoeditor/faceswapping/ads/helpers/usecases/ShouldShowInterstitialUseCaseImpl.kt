package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import android.util.Log
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.AppPrefsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import javax.inject.Inject

class ShouldShowInterstitialUseCaseImpl @Inject constructor(
    private val adControlConfigManager: AdControlConfigManager,
    private val prefsManager: AppPrefsManager
) : ShouldShowInterstitialUseCase {
    companion object {
        private const val TAG_INTER = "InterstitialTrace"
    }

    override operator fun invoke(
        screen: String,
        trigger: String,
        noCounterNeeded: Boolean
    ): Boolean {
        Log.d(TAG_INTER, "shouldShow start screen=$screen trigger=$trigger noCounterNeeded=$noCounterNeeded")
        if (!adControlConfigManager.isInterstitialEnabledForTrigger(screen, trigger)) {
            Log.d(TAG_INTER, "blocked: interstitial disabled for trigger")
            return false
        }

        // Pre-home screens must bypass counter logic by requirement.
        if (noCounterNeeded || adControlConfigManager.isPreHomeScreen(screen)) {
            Log.d(TAG_INTER, "allowed: bypass counter logic")
            return true
        }

        val cooldownSeconds = adControlConfigManager.getInterstitialCooldownSeconds()
        if (cooldownSeconds > 0) {
            val lastShownAt = prefsManager.getLastInterstitialShownAtMillis()
            if (lastShownAt > 0L) {
                val elapsedSeconds = (System.currentTimeMillis() - lastShownAt) / 1000
                if (elapsedSeconds < cooldownSeconds) {
                    Log.d(TAG_INTER, "blocked: cooldown active elapsed=$elapsedSeconds cooldown=$cooldownSeconds")
                    return false
                }
            }
        }

        // Home first-click behavior gate.
        if (screen == "HomeFragmentScreen" || screen == "DashboardFragmentScreen" || screen == "MainScreen") {
            if (prefsManager.isFirstHomeInterstitialClickPending()) {
                prefsManager.markFirstHomeInterstitialClickConsumed()
                val allowed = adControlConfigManager.isInterFirstCountEnabledForHome()
                Log.d(TAG_INTER, "home first-click gate allowed=$allowed")
                return allowed
            }
        }

        val currentCounter = prefsManager.getAdCounter()
        val nextCounter = currentCounter + 1

        prefsManager.incrementAdCounter()
        val allowedByThreshold = adControlConfigManager.isInterstitialThresholdReached(nextCounter)
        Log.d(TAG_INTER, "counter gate current=$currentCounter next=$nextCounter allowed=$allowedByThreshold")
        return allowedByThreshold
    }
}
