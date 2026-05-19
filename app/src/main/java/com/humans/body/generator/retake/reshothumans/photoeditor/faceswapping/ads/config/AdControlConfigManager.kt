package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.BuildConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models.AdControlConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models.RemoteAdIdsConfig
import kotlinx.serialization.json.Json
import timber.log.Timber

class AdControlConfigManager(
    firebaseRemoteConfig: FirebaseRemoteConfig
) : BaseRemoteConfigManager<AdControlConfig>(firebaseRemoteConfig, "Config_v18") {

    companion object {
        private const val TAG_CFG = "ConfigTrace"
        private val jsonParser by lazy {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
    }

    init {
        val timeOut = if (BuildConfig.DEBUG) 0 else 300
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(timeOut.toLong())
            .build()

        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            .addOnFailureListener {
                Timber.e(it, "Remote Config settings failed")
            }
    }

    fun fetchAdConfig() {
        fetchConfig()
    }

    override fun parseJson(json: String): AdControlConfig? {
        Log.d(TAG_CFG, "AdControl parseJson start length=${json.length}")
        return try {
            val parsed = jsonParser.decodeFromString<AdControlConfig>(json)
            Log.d(TAG_CFG,
                "AdControl parseJson success appNull=${parsed.app == null} adsAppOpenEnabled=${parsed.ads.appOpen.enabled} adsAppOpenResume=${parsed.ads.appOpen.resume} rewardedEnabledV2=${parsed.rewarded?.enabled} rewardedEnabledLegacy=${parsed.ads.rewarded.enabled} rewardedBeforePremiumV2=${parsed.rewarded?.rewardedAdsBeforePremium} rewardedBeforePremiumLegacy=${parsed.ads.rewarded.rewardedAdsBeforePremium}"
            )
            parsed
        } catch (e: Exception) {
            Log.e(TAG_CFG, "AdControl parseJson serialization error: ${e.localizedMessage}")
            null
        }
    }

    override fun postProcessParsedData(json: String) {
        val cfg = getConfigV2()
        Log.d(TAG_CFG,
            "AdControl postProcess appAds=${cfg?.app?.ads} appOpenSplash=${cfg?.app?.appOpen?.splash} appOpenResume=${cfg?.app?.appOpen?.resume} minBgSec=${getResumeMinBackgroundSeconds()} shouldResume=${shouldShowAppOpenResume()} shouldSplash=${shouldShowAppOpenSplash()} rewardedEnabled=${isRewardedEnabled()} rewardedBeforePremium=${getRewardedAdsBeforePremium()} rawPrefix=${json.take(180)}"
        )
    }

    private fun getConfigV2() = configData

    private fun getRemoteAdIds(): RemoteAdIdsConfig? = configData?.adIds ?: configData?.adIdsSnake

    private fun resolveFromMap(ids: Map<String, String>?, fallbackIds: Map<String, String>?, vararg keys: String): String {
        keys.forEach { key ->
            val value = ids?.get(key)?.trim().orEmpty().ifEmpty { fallbackIds?.get(key)?.trim().orEmpty() }
            if (value.isNotEmpty()) return value
        }
        val defaultValue = ids?.get("default")?.trim().orEmpty().ifEmpty { fallbackIds?.get("default")?.trim().orEmpty() }
        if (defaultValue.isNotEmpty()) return defaultValue
        return ids?.values?.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
            .ifEmpty { fallbackIds?.values?.firstOrNull { it.isNotBlank() }?.trim().orEmpty() }
    }

    private fun resolveProdAdId(configuredId: String?, fallbackProdId: String): String {
        if (BuildConfig.DEBUG) return fallbackProdId
        val candidate = configuredId?.trim().orEmpty()
        return candidate.ifEmpty { fallbackProdId }
    }

    fun getProdBannerAdUnitId(fallbackProdId: String): String {
        val ids = getRemoteAdIds()?.banner
        return resolveProdAdId(resolveFromMap(ids, HardcodedAdIds.banner, "default"), fallbackProdId)
    }

    fun getProdAppOpenSplashAdUnitId(fallbackProdId: String): String {
        val ids = getRemoteAdIds()?.appOpen
        return resolveProdAdId(resolveFromMap(ids, HardcodedAdIds.appOpen, "splash", "default"), fallbackProdId)
    }

    fun getProdAppOpenResumeAdUnitId(fallbackProdId: String): String {
        val ids = getRemoteAdIds()?.appOpen
        return resolveProdAdId(resolveFromMap(ids, HardcodedAdIds.appOpen, "resume", "default"), fallbackProdId)
    }

    fun getProdInterstitialAdUnitId(screen: String, fallbackProdId: String): String {
        val ids = getRemoteAdIds()?.interstitial
        val configuredId = when (screen) {
            RemoteScreens.SPLASH_SCREEN -> resolveFromMap(ids, HardcodedAdIds.interstitial, "splash", "default")
            RemoteScreens.INTRO_SCREEN -> resolveFromMap(ids, HardcodedAdIds.interstitial, "intro", "onboarding", "default")
            RemoteScreens.LANGUAGE_SCREEN -> resolveFromMap(ids, HardcodedAdIds.interstitial, "language", "default")
            else -> resolveFromMap(ids, HardcodedAdIds.interstitial, screen, "default")
        }
        return resolveProdAdId(configuredId, fallbackProdId)
    }

    fun getProdNativeAdUnitId(screen: String, fallbackProdId: String, position: String? = null): String {
        val ids = getRemoteAdIds()?.nativeIds
        val normalizedPosition = position?.trim()?.lowercase().orEmpty()
        val configuredId = when (screen) {
            RemoteScreens.SPLASH_SCREEN -> when (normalizedPosition) {
                "top" -> resolveFromMap(ids, HardcodedAdIds.nativeIds, "splash_top", "splash", "default")
                "bottom" -> resolveFromMap(ids, HardcodedAdIds.nativeIds, "splash_bottom", "splash", "default")
                else -> resolveFromMap(ids, HardcodedAdIds.nativeIds, "splash", "default")
            }
            RemoteScreens.INTRO_SCREEN -> resolveFromMap(ids, HardcodedAdIds.nativeIds, "intro", "onboarding", "default")
            RemoteScreens.LANGUAGE_SCREEN -> resolveFromMap(ids, HardcodedAdIds.nativeIds, "language", "default")
            else -> resolveFromMap(ids, HardcodedAdIds.nativeIds, screen, "default")
        }
        return resolveProdAdId(configuredId, fallbackProdId)
    }

    fun getProdRewardedAdUnitId(fallbackProdId: String): String {
        val ids = getRemoteAdIds()?.rewarded
        return resolveProdAdId(resolveFromMap(ids, HardcodedAdIds.rewarded, "default"), fallbackProdId)
    }

    fun getProdInitialRewardedAdUnitId(fallbackProdId: String): String {
        val ids = getRemoteAdIds()?.rewarded
        return resolveProdAdId(resolveFromMap(ids, HardcodedAdIds.rewarded, "initial", "default"), fallbackProdId)
    }

    fun shouldShowScreen(screenName: String, isFirstLaunch: Boolean): Boolean {
        val screensV2 = if (isFirstLaunch) {
            configData?.app?.screens?.first
        } else {
            configData?.app?.screens?.second
        }
        val screens = if (screensV2 != null) {
            screensV2
        } else if (isFirstLaunch) {
            configData?.showScreens?.firstLaunch
        } else {
            configData?.showScreens?.subsequentLaunches
        }

        if (screens == null) {
            return when (screenName) {
                "splash", "languages" -> true
                "intro", "premium" -> isFirstLaunch
                else -> false
            }
        }

        return when (screenName) {
            "splash" -> screens?.splash == 1
            "languages" -> screens?.languages == 1
            "intro" -> screens?.intro == 1
            "premium" -> screens?.premium == 1
            else -> false
        }
    }

    fun shouldShowLanguagesFirst() = shouldShowScreen("languages", true)
    fun shouldShowIntroFirst() = shouldShowScreen("intro", true)
    fun shouldShowPremiumFirst() = shouldShowScreen("premium", true)

    fun shouldShowLanguagesSecond() = shouldShowScreen("languages", false)
    fun shouldShowIntroSecond() = shouldShowScreen("intro", false)
    fun shouldShowPremiumSecond() = shouldShowScreen("premium", false)
    fun shouldShowAppOpen(): Boolean {
        if (configData == null) return true
        return (configData?.app?.ads ?: 1) == 1 && (
        configData?.app?.appOpen?.resume == 1 || configData?.ads?.appOpen?.enabled == 1
        )
    }
    fun shouldShowAppOpenSplash(): Boolean {
        if (configData == null) return true
        return (configData?.app?.ads ?: 1) == 1 && (
        configData?.app?.appOpen?.splash == 1 || configData?.ads?.appOpen?.splash == 1
        )
    }
    fun shouldShowAppOpenResume(): Boolean {
        if (configData == null) return true
        return (configData?.app?.ads ?: 1) == 1 && (
        configData?.app?.appOpen?.resume == 1 || configData?.ads?.appOpen?.resume == 1
        )
    }
    fun getResumeMinBackgroundSeconds(): Int =
        configData?.app?.appOpen?.resumeMinBackgroundSeconds
            ?: configData?.ads?.appOpen?.resumeMinBackgroundSeconds
            ?: 0

    fun getAppOpenShowAfter(position: String): Int {
        val v2 = configData?.app?.appOpen
        val legacy = configData?.ads?.appOpen
        val value = when (position.lowercase()) {
            "splash" -> v2?.splashShowAfter ?: v2?.showAfter
                ?: legacy?.splashShowAfter ?: legacy?.showAfter
            "resume" -> v2?.resumeShowAfter ?: v2?.showAfter
                ?: legacy?.resumeShowAfter ?: legacy?.showAfter
            else -> v2?.showAfter ?: legacy?.showAfter
        }
        return (value ?: 1).coerceAtLeast(1)
    }

    fun getAppOpenLimit(position: String): Int {
        val v2 = configData?.app?.appOpen
        val legacy = configData?.ads?.appOpen
        val value = when (position.lowercase()) {
            "splash" -> v2?.splashLimit ?: v2?.appOpenLimit
                ?: legacy?.splashLimit ?: legacy?.appOpenLimit
            "resume" -> v2?.resumeLimit ?: v2?.appOpenLimit
                ?: legacy?.resumeLimit ?: legacy?.appOpenLimit
            else -> v2?.appOpenLimit ?: legacy?.appOpenLimit
        }
        return value ?: Int.MAX_VALUE
    }

    fun isBannerVisible(activityName: String, position: String): Boolean {
        if ((configData?.app?.ads ?: 1) != 1) return false

        val v2 = resolveBannerPlacementValue(activityName, position)
        if (v2 != null) return v2 > 0

        val placementNew = findBannerPlacement(activityName)
        if (placementNew == null) {
            // Default when config is absent:
            // top banners OFF, all other positions OFF unless explicitly configured.
            return false
        }

        return when (position.lowercase()) {
            "top" -> (placementNew?.get("top") ?: 0) > 0
            "bottom" -> (placementNew?.get("bottom") ?: 0) > 0
            else -> false
        }
    }

    fun getBannerType(activityName: String, position: String): String {
        val v2 = resolveBannerPlacementValue(activityName, position)
        if (v2 != null) return "a"

        val placementNew = findBannerPlacement(activityName)
        val typeInt = when (position.lowercase()) {
            "top" -> placementNew?.get("top") ?: 0
            "bottom" -> placementNew?.get("bottom") ?: 0
            else -> 0
        }
        return when (typeInt) {
            1 -> "a"
            2 -> "c"
            3 -> "r"
            else -> "a"
        }
    }

    fun isInterstitialEnabledForTrigger(screen: String, trigger: String): Boolean {
        if (configData == null) {
            Log.d(TAG_CFG, "Interstitial gate default ON (config missing) screen=$screen trigger=$trigger")
            return true
        }
        val v2 = resolveInterstitialPlacementValue(screen, trigger)
        if (v2 != null) {
            val enabled = isInterstitialEnabled() && v2 == 1
            Log.d(TAG_CFG, "Interstitial gate v2 screen=$screen trigger=$trigger placement=$v2 enabled=$enabled")
            return enabled
        }

        val interstitialNew = getConfigV2()?.ads?.interstitial
        val fallbackEnabled = interstitialNew?.enabled == 1 && interstitialNew.screens[screen]?.get(trigger) == 1
        Log.d(TAG_CFG, "Interstitial gate fallback screen=$screen trigger=$trigger enabled=$fallbackEnabled")
        return fallbackEnabled
    }

    private fun findBannerPlacement(screenName: String): Map<String, Int>? {
        val placements = getConfigV2()?.ads?.banner?.placements ?: return null
        for (candidate in screenCandidates(screenName)) {
            placements[candidate]?.let { return it }
        }
        return null
    }

    private fun screenCandidates(screenName: String): List<String> {
        val candidates = linkedSetOf(screenName)
        if (screenName.endsWith("FragmentScreen")) {
            candidates += screenName.replace("FragmentScreen", "Screen")
            candidates += screenName.removeSuffix("FragmentScreen")
        }
        when (screenName) {
            "HomeFragmentScreen", "DashboardFragmentScreen" -> candidates += "MainScreen"
            "MenuFragmentScreen" -> candidates += "SettingsScreen"
        }
        return candidates.toList()
    }

    fun isInterstitialThresholdReached(counter: Int): Boolean {
        val showAfter = getInterstitialClickInterval()
        return counter >= showAfter
    }

    fun isInterstitialEnabled(): Boolean {
        if (configData == null) return true
        return (configData?.app?.ads ?: 1) == 1 && (
            configData?.interstitial?.enabled == 1 ||
                configData?.ads?.interstitial?.enabled == 1
            )
    }

    fun getInterstitialClickInterval(): Int {
        return configData?.interstitial?.clickInterval
            ?: configData?.ads?.interstitial?.clickInterval
            ?: configData?.ads?.interstitial?.showAfter
            ?: 2
    }

    fun isInterFirstCountEnabledForHome(): Boolean {
        return (configData?.interstitial?.isInterFirstCount
            ?: configData?.ads?.interstitial?.isInterFirstCount
            ?: 1) == 1
    }

    fun getInterstitialCooldownSeconds(): Int {
        return configData?.interstitial?.cooldownSeconds
            ?: configData?.ads?.interstitial?.cooldownSeconds
            ?: 0
    }

    fun isPreHomeScreen(screen: String): Boolean {
        return screen in setOf("SplashScreen", "LanguagesScreen", "IntroScreen", "PremiumScreen")
    }

    fun isRewardedEnabled(): Boolean {
        // Default: rewarded is ON when config has not arrived yet.
        if (configData == null) return true
        return (configData?.app?.ads ?: 1) == 1 && (
            configData?.rewarded?.enabled == 1 ||
                configData?.ads?.rewarded?.enabled == 1
            )
    }

    fun getRewardedAdsBeforePremium(): Int {
        return configData?.rewarded?.rewardedAdsBeforePremium
            ?: configData?.ads?.rewarded?.rewardedAdsBeforePremium
            ?: 2
    }

    fun isRewardedEnabledForPlacement(screen: String, trigger: String): Boolean {
        if (!isRewardedEnabled()) return false
        val value = resolveRewardedPlacementValue(screen, trigger)
        val enabled = value == null || value == 1
        Log.d(
            TAG_CFG,
            "Rewarded gate screen=$screen trigger=$trigger placement=$value enabled=$enabled"
        )
        return enabled
    }

    private fun resolveBannerPlacementValue(screen: String, position: String): Int? {
        val cfg = configData?.banner ?: return null
        if (cfg.enabled != 1) return 0
        for (candidate in placementCandidatesForScreenPosition(screen, position)) {
            cfg.placements[candidate]?.let { return it }
        }
        return null
    }

    private fun resolveInterstitialPlacementValue(screen: String, trigger: String): Int? {
        val cfg = configData?.interstitial ?: return null
        if (cfg.enabled != 1) return 0
        for (candidate in placementCandidatesForScreenTrigger(screen, trigger)) {
            cfg.placements[candidate]?.let { return it }
        }
        return null
    }

    private fun resolveRewardedPlacementValue(screen: String, trigger: String): Int? {
        val v2 = configData?.rewarded
        if (v2 != null) {
            if (v2.enabled != 1) return 0
            for (candidate in placementCandidatesForScreenTrigger(screen, trigger)) {
                v2.placements[candidate]?.let { return it }
            }
            return null
        }
        val legacy = configData?.ads?.rewarded ?: return null
        if (legacy.enabled != 1) return 0
        for (candidate in placementCandidatesForScreenTrigger(screen, trigger)) {
            legacy.placements[candidate]?.let { return it }
        }
        return null
    }

    private fun placementCandidatesForScreenPosition(screen: String, position: String): List<String> {
        val baseCandidates = screenCandidates(screen)
        val normalizedPosition = normalizeKey(position)
        val out = linkedSetOf<String>()
        baseCandidates.forEach { base ->
            out += "${normalizeKey(base)}_$normalizedPosition"
            out += "${normalizeKey(base.removeSuffix("Screen"))}_$normalizedPosition"
            out += "${normalizeKey(base.removeSuffix("FragmentScreen"))}_$normalizedPosition"
        }
        return out.toList()
    }

    private fun placementCandidatesForScreenTrigger(screen: String, trigger: String): List<String> {
        val normalizedTrigger = normalizeKey(trigger)
        val out = linkedSetOf<String>()
        screenCandidates(screen).forEach { base ->
            // Keep both the full normalized base and stripped variants, e.g.
            // HomeFragmentScreen -> home_fragment_screen + home_fragment + home
            val fullBase = normalizeKey(base)
            val withoutScreen = normalizeKey(base.removeSuffix("Screen"))
            val withoutFragmentScreen = normalizeKey(base.removeSuffix("FragmentScreen"))
            val compactBase = normalizeKey(base.removeSuffix("FragmentScreen").removeSuffix("Screen"))

            out += "${fullBase}_$normalizedTrigger"
            out += "${withoutScreen}_$normalizedTrigger"
            out += "${withoutFragmentScreen}_$normalizedTrigger"
            out += "${compactBase}_$normalizedTrigger"
        }
        out += normalizedTrigger
        when (screen) {
            "SplashScreen" -> out += "splash"
            "LanguagesScreen" -> {
                out += "language_first_open"
                out += "language_second_open"
                out += "home_language"
            }
            "IntroScreen" -> {
                if (normalizedTrigger == "next" || normalizedTrigger == "skip") out += "intro_next"
            }
            "PremiumScreen" -> {
                out += "premium_close"
                out += "purchase_close"
                out += "purchase_continue"
            }
        }
        Log.d(TAG_CFG, "Interstitial candidates screen=$screen trigger=$trigger candidates=$out")
        return out.toList()
    }

    private fun normalizeKey(value: String): String {
        return value
            .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
            .replace("-", "_")
            .replace(" ", "_")
            .lowercase()
    }
}
