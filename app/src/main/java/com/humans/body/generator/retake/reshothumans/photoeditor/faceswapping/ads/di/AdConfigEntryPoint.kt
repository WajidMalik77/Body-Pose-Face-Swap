package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.AppPrefsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface AdConfigEntryPoint {
    fun adControlConfigManager(): AdControlConfigManager
    fun appPrefsManager(): AppPrefsManager
}
