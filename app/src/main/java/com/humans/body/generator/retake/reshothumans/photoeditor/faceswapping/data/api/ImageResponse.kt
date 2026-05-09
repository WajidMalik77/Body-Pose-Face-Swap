package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api
import androidx.annotation.Keep

@Keep
data class ImageResponse(
    val url: String?,
    val seed: Int?,
    val cost: Double?

)

@Keep
data class ImageToImageResponse(
    val image: String?,                 // base64
    val images: List<ImageItem>?,
    val seed: Int?,
    val cost: Double?
)

@Keep
data class ImageItem(
    val image: String
)