package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RemoteNativeConfigWrapper(
    @SerialName("native_ads")
    val native_ads: NativeAdSettingsConfig = NativeAdSettingsConfig()
)

@Keep
@Serializable
data class NativeAdSettingsConfig(
    val enabled: Int = 1,
    val preload: Int = 0,
    @SerialName("default_style")
    val default_config: NativeAdUnitConfig = NativeAdUnitConfig(),
    val placements: Map<String, NativePlacement> = emptyMap()
)

@Keep
@Serializable
data class NativePlacement(
    val value: Int? = null,
    val top: Int? = null,
    var bottom: Int? = null,
    val center: Int? = null,
    val style: NativeAdColorConfig? = null,
    var fullScreen: Int? = null,
    var fullScreen1: Int? = null,
    var fullScreen2: Int? = null,
    val recycler: Int? = null
)

@Keep
@Serializable
data class NativeAdUnitConfig(
    val enabled: Int? = null,
    val preload: Int? = null,
    val size: Int? = null,
    @SerialName("show_after")
    val showAfter: Int? = null,
    @SerialName("native_limit")
    val nativeLimit: Int? = null,
    @SerialName("color_config")
    val color_config: NativeAdColorConfig? = null
)

@Keep
@Serializable
data class NativeAdColorConfig(
    val mode: Int? = null,
    @SerialName("background_color")
    val backgroundColorHex: String? = null,
    @SerialName("corner_radius_dp")
    val cornerRadiusDp: Int? = null,
    @SerialName("stroke_width_dp")
    val strokeWidthDp: Int? = null,
    @SerialName("stroke_color")
    val strokeColorHex: String? = null,
    @SerialName("headline_color")
    val headlineColorHex: String? = null,
    @SerialName("body_text_color")
    val bodyTextColorHex: String? = null,
    @SerialName("cta_background_color")
    val ctaBackgroundColorHex: String? = null,
    @SerialName("cta_text_color")
    val ctaTextColorHex: String? = null,
    @SerialName("cta_corner_radius_dp")
    val ctaCornerRadiusDp: Int? = null,
    @SerialName("cta_text_size_sp")
    val ctaTextSizeSp: Int? = null,
    // v2 compatibility keys
    @SerialName("DarkBackgroundColor")
    val darkBackgroundColorHex: String? = null,
    @SerialName("LightBackgroundColor")
    val lightBackgroundColorHex: String? = null,
    @SerialName("DarkStrokeColor")
    val darkStrokeColorHex: String? = null,
    @SerialName("LightStrokeColor")
    val lightStrokeColorHex: String? = null,
    @SerialName("DarkHeadlineColor")
    val darkHeadlineColorHex: String? = null,
    @SerialName("LightHeadlineColor")
    val lightHeadlineColorHex: String? = null,
    @SerialName("DarkBodyColor")
    val darkBodyColorHex: String? = null,
    @SerialName("LightBodyColor")
    val lightBodyColorHex: String? = null,
    @SerialName("DarkCtaBackgroundColor")
    val darkCtaBackgroundColorHex: String? = null,
    @SerialName("LightCtaBackgroundColor")
    val lightCtaBackgroundColorHex: String? = null,
    @SerialName("DarkCtaTextColor")
    val darkCtaTextColorHex: String? = null,
    @SerialName("LightCtaTextColor")
    val lightCtaTextColorHex: String? = null,
    @SerialName("corner_radius")
    val cornerRadius: Int? = null,
    @SerialName("stroke_width")
    val strokeWidth: Int? = null,
    @SerialName("cta_radius")
    val ctaRadius: Int? = null,
    @SerialName("cta_text_size")
    val ctaTextSize: Int? = null,
    @SerialName("cta_bg_color")
    val ctaBgColorHex: String? = null
)
