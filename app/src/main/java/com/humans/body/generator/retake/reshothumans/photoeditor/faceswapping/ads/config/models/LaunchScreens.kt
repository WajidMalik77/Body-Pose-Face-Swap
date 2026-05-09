package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.Serializable

@Serializable
data class LaunchScreens(
    val splash: Int = 1,
    val languages: Int? = null,
    val intro: Int? = null,
    val premium: Int? = null
)
