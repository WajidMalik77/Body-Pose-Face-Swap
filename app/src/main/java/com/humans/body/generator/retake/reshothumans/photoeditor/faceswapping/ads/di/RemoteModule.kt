package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.AdControlConfigManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.NativeAdConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

    @Provides
    @Singleton
    fun provideAdControlConfigManager(remoteConfig: FirebaseRemoteConfig): AdControlConfigManager {
        return AdControlConfigManager(remoteConfig)
    }

    @Provides
    @Singleton
    fun provideNativeAdConfigManager(remoteConfig: FirebaseRemoteConfig): NativeAdConfigManager {
        return NativeAdConfigManager(remoteConfig)
    }
}