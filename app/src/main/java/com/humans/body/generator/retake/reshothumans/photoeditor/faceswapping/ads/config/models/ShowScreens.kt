package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowScreens(
    @SerialName("first_launch")
    val firstLaunch: LaunchScreens = LaunchScreens(),
    @SerialName("subsequent_launches")
    val subsequentLaunches: LaunchScreens = LaunchScreens()
)