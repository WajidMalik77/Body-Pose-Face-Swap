package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/imagen-4.0-generate-001:predict")
    fun generateImage(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: ImagenRequest
    ): Call<ImagenResponse>
}


data class ImagenRequest(
    val instances: List<Instance>,
    val parameters: Parameters? = null
)

data class Instance(
    val prompt: String
)

data class Parameters(
    val sampleCount: Int = 1
)

data class ImagenResponse(
    val predictions: List<Prediction>
)

data class Prediction(
    val bytesBase64Encoded: String
)



fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

