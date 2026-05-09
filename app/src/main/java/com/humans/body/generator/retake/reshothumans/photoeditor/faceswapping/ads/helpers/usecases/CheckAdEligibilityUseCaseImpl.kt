package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import android.content.Context
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.AdEligibility
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import timber.log.Timber
import javax.inject.Inject

class CheckAdEligibilityUseCaseImpl @Inject constructor(
    private val adsPref: AdsPref,
    private val connectivityChecker: ConnectivityChecker
) : CheckAdEligibilityUseCase {

    override suspend operator fun invoke(context: Context): AdEligibility {
        return when {
            adsPref.getIsPremiumStatus() ->
                AdEligibility(false, "Premium user")
            !connectivityChecker.isConnected(context) ->
                AdEligibility(false, "No internet")
            else -> AdEligibility(true).also { Timber.d("Ad eligibility true") }
        }
    }
}
