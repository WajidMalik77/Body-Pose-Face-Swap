package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LegalConfig(
    @SerialName("privacy_policy")
    val privacyPolicy: String = "",
    @SerialName("terms_of_use")
    val termsOfUse: String = "",
    @SerialName("feedback_mail")
    val feedbackMail: String = ""
)
