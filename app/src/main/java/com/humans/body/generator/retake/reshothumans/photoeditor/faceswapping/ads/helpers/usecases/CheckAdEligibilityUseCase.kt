package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import android.content.Context
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.AdEligibility

interface CheckAdEligibilityUseCase {
    suspend operator fun invoke(context: Context): AdEligibility
}