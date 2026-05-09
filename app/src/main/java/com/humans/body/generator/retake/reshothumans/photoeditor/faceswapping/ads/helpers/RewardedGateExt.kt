package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers

import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di.AdConfigEntryPoint
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.dialogs.LoadingDialog
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.RewardedAdsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

private const val TAG_REWARDED = "RewardedTrace"

fun Fragment.runWithRewardedGate(screen: String, trigger: String, onAllowed: () -> Unit) {
    if (!isAdded) return
    val ctx = requireContext()

    if (PrefUtil.isPremium(ctx)) {
        onAllowed()
        return
    }

    val activity = activity ?: run {
        startActivity(Intent(ctx, PremiumActivity::class.java))
        return
    }
    val configEntryPoint = EntryPointAccessors.fromActivity(activity, AdConfigEntryPoint::class.java)
    val adControlConfigManager = configEntryPoint.adControlConfigManager()
    val appPrefsManager = configEntryPoint.appPrefsManager()

    val generationClicks = appPrefsManager.incrementGenerationClickCountAndGet()
    Log.d(
        TAG_REWARDED,
        "generation click count=$generationClicks screen=$screen trigger=$trigger"
    )

    if (!adControlConfigManager.isRewardedEnabledForPlacement(screen, trigger)) {
        Log.d(TAG_REWARDED, "blocked: rewarded disabled for placement screen=$screen trigger=$trigger")
        startActivity(Intent(ctx, PremiumActivity::class.java))
        return
    }
    val maxBeforePremium = adControlConfigManager.getRewardedAdsBeforePremium()
    val currentGateCount = appPrefsManager.getRewardedGateCount()
    if (currentGateCount >= maxBeforePremium) {
        Log.d(TAG_REWARDED, "blocked: max rewarded generations reached current=$currentGateCount max=$maxBeforePremium")
        startActivity(Intent(ctx, PremiumActivity::class.java))
        return
    }

    val rewardedManager = RewardedAdsManager.getInstance(AdsPref(ctx.applicationContext))
    val loadingDialog = LoadingDialog(activity)
    val loadingStartedAt = System.currentTimeMillis()
    loadingDialog.show()
    rewardedManager.preload(
        context = ctx,
        onLoaded = {
            Log.d(TAG_REWARDED, "loaded: screen=$screen trigger=$trigger current=$currentGateCount max=$maxBeforePremium")
            viewLifecycleOwner.lifecycleScope.launch {
                val elapsed = System.currentTimeMillis() - loadingStartedAt
                val remaining = max(0L, 800L - elapsed)
                if (remaining > 0L) delay(remaining)
                loadingDialog.dismiss()
                if (!isAdded || activity.isFinishing || activity.isDestroyed) return@launch
                rewardedManager.show(
                    activity = activity,
                    onRewardEarned = {
                        val newCount = appPrefsManager.incrementRewardedGateCountAndGet()
                        Log.d(TAG_REWARDED, "reward earned: newCount=$newCount screen=$screen trigger=$trigger")
                        onAllowed()
                    },
                    onFailedOrDismissedWithoutReward = {
                        Log.d(TAG_REWARDED, "failed or dismissed without reward screen=$screen trigger=$trigger")
                        if (isAdded) startActivity(Intent(requireContext(), PremiumActivity::class.java))
                    }
                )
            }
        },
        onFailed = {
            loadingDialog.dismiss()
            Log.d(TAG_REWARDED, "preload failed screen=$screen trigger=$trigger reason=$it")
            if (isAdded) startActivity(Intent(requireContext(), PremiumActivity::class.java))
        }
    )
}
