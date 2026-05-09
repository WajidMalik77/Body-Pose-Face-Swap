package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

interface ShouldShowInterstitialUseCase {
    operator fun invoke(
        screen: String,
        trigger: String,
        noCounterNeeded: Boolean
    ): Boolean
}