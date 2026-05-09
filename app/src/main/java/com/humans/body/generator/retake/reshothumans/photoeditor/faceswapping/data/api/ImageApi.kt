package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApi {

    // ── Text to Image ──────────────────────────────────────────
    @POST("v1/flux-schnell/text-to-image")
    fun generateImage(
        @Header("Authorization") authHeader: String,
        @Body body: ImageRequest
    ): Call<ImageResponse>

    // ── Image to Image (XL multipart) ──────────────────────────
    @Multipart
    @POST("v1/stable-diffusion-xl/image-to-image")
    fun generateImageToImage(
        @Header("Authorization") authHeader: String,
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Call<ImageResponse>

    // ── Image to Image (SD JSON) ───────────────────────────────
    @POST("v1/stable-diffusion/image-to-image")
    fun imageToImage(
        @Header("Authorization") authHeader: String,
        @Body request: ImageToImageRequest
    ): Call<ImageToImageResponse>

    // ── Remove Background — getimg.ai tools endpoint ───────────
    @POST("v1/stable-diffusion/remove-background")
    suspend fun removeBackground(
        @Header("Authorization") auth: String,  // "Bearer YOUR_KEY"
        @Body body: BgRemoveRequest
    ): Response<BgRemoveResponse>

    @Multipart
    @POST("v1/stable-diffusion/remove-background")
    fun removeBg(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part,
        @Part("size") size: RequestBody
    ): Call<ResponseBody>
}

// ── Request / Response models ──────────────────────────────────

data class ImageToImageRequest(
    val prompt: String,
    val image: String,                 // Base64 source image
    val strength: Float = 0.35f,       // 0.0 = keep original, 1.0 = fully new
    val output_format: String = "png"
)

data class BgRemoveRequest(
    @SerializedName("model") val model: String = "background-removal",
    @SerializedName("image") val image: String  // Base64 source image
)

data class BgRemoveResponse(
    @SerializedName("image") val image: String? // Base64 result image
)