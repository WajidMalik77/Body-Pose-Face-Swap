package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.app

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models.PremiumState
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepository @Inject constructor(
    private val adsPref: AdsPref
) {
    private val _premiumState = MutableStateFlow(PremiumState.UNKNOWN)
    val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()

    fun updatePremiumState(isPremium: Boolean) {
        _premiumState.value = if (isPremium) PremiumState.PREMIUM else PremiumState.FREE
    }

    fun isPremiumUser(): Boolean {
        return adsPref.getIsPremiumStatus()
    }
}