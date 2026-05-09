package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui.AdsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface AdsManagerEntryPoint {
    fun adsManager(): AdsManager
}