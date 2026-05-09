package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api
import androidx.annotation.Keep

@Keep
data class ImageRequest(
    val prompt: String,
    val response_format: String = "url"

)

