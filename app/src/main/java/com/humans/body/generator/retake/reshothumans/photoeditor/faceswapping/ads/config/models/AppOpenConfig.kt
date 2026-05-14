package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.Serializable

@Serializable
data class AppOpenConfig(
    val enabled: Int = 1,
    val splash: Int = 1,
    val resume: Int = 1,
    @kotlinx.serialization.SerialName("resume_min_background_seconds")
    val resumeMinBackgroundSeconds: Int = 0,
    @kotlinx.serialization.SerialName("show_after")
    val showAfter: Int? = null,
    @kotlinx.serialization.SerialName("splash_show_after")
    val splashShowAfter: Int? = null,
    @kotlinx.serialization.SerialName("resume_show_after")
    val resumeShowAfter: Int? = null,
    @kotlinx.serialization.SerialName("app_open_limit")
    val appOpenLimit: Int? = null,
    @kotlinx.serialization.SerialName("splash_limit")
    val splashLimit: Int? = null,
    @kotlinx.serialization.SerialName("resume_limit")
    val resumeLimit: Int? = null
)
