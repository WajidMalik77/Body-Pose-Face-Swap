package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.di

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.AppPrefsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui.AdsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui.BannerAdOrchestrator
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui.InterstitialAdOrchestrator
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.ui.NativeAdOrchestrator
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.AdConfigRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.AdConfigRepositoryImpl
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.BannerAdRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.BannerAdRepositoryImpl
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.CheckAdEligibilityUseCaseImpl
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.ConnectivityChecker
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.ConnectivityCheckerImpl
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.NativeAdRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.NativeAdRepositoryImpl
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.ShouldShowInterstitialUseCase
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases.ShouldShowInterstitialUseCaseImpl
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.AdmobNativeManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.managers.BannerAdsManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.AdsPref
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsDomainModule {

    @Binds
    @Singleton
    abstract fun bindCheckAdEligibilityUseCase(
        impl: CheckAdEligibilityUseCaseImpl
    ): CheckAdEligibilityUseCase

    @Binds
    @Singleton
    abstract fun bindShouldShowInterstitialUseCase(
        impl: ShouldShowInterstitialUseCaseImpl
    ): ShouldShowInterstitialUseCase

    @Binds
    @Singleton
    abstract fun bindConnectivityChecker(
        impl: ConnectivityCheckerImpl
    ): ConnectivityChecker
}

// Data Module
@Module
@InstallIn(SingletonComponent::class)
abstract class AdsDataModule {

    @Binds
    @Singleton
    abstract fun bindAdConfigRepository(
        impl: AdConfigRepositoryImpl
    ): AdConfigRepository

    @Binds
    @Singleton
    abstract fun bindBannerAdRepository(
        impl: BannerAdRepositoryImpl
    ): BannerAdRepository

    @Binds
    @Singleton
    abstract fun bindNativeAdRepository(
        impl: NativeAdRepositoryImpl
    ): NativeAdRepository
}

// Presentation Module
@Module
@InstallIn(ActivityComponent::class)
object AdsPresentationModule {

    @Provides
    @ActivityScoped
    fun provideAdsManager(
        adConfigRepository: AdConfigRepository,
        bannerAdOrchestrator: BannerAdOrchestrator,
        nativeAdOrchestrator: NativeAdOrchestrator,
        interstitialAdOrchestrator: InterstitialAdOrchestrator,
        checkEligibility: CheckAdEligibilityUseCase
    ): AdsManager {
        return AdsManager(
            adConfigRepository,
            bannerAdOrchestrator,
            nativeAdOrchestrator,
            interstitialAdOrchestrator,
            checkEligibility
        )
    }

    @Provides
    @ActivityScoped
    fun provideBannerAdOrchestrator(
        bannerAdRepository: BannerAdRepository,
        adConfigRepository: AdConfigRepository,
        @ActivityBanner bannerAdsManager: BannerAdsManager,
        checkEligibility: CheckAdEligibilityUseCase
    ): BannerAdOrchestrator {
        return BannerAdOrchestrator(
            bannerAdRepository,
            adConfigRepository,
            bannerAdsManager,
            checkEligibility
        )
    }

    @Provides
    @ActivityScoped
    fun provideNativeAdOrchestrator(
        nativeAdRepository: NativeAdRepository,
        adConfigRepository: AdConfigRepository,
        @ActivityNative admobNativeManager: AdmobNativeManager,
        checkEligibility: CheckAdEligibilityUseCase,
        adsPref: AdsPref
    ): NativeAdOrchestrator {
        return NativeAdOrchestrator(
            nativeAdRepository,
            adConfigRepository,
            admobNativeManager,
            checkEligibility,
            adsPref
        )
    }

    @Provides
    @ActivityScoped
    fun provideInterstitialAdOrchestrator(
        adsPref: AdsPref,
        prefsManager: AppPrefsManager,
        adConfigRepository: AdConfigRepository,
        shouldShowInterstitial: ShouldShowInterstitialUseCase,
        checkEligibility: CheckAdEligibilityUseCase
    ): InterstitialAdOrchestrator {
        return InterstitialAdOrchestrator(
            adsPref,
            prefsManager,
            adConfigRepository,
            shouldShowInterstitial,
            checkEligibility
        )
    }
}
