package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import retrofit2.http.*

interface GeminiApiService {

    @GET("v1beta/models")
    suspend fun getAvailableModels(
        @Query("key") apiKey: String
    ): ModelsResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") modelName: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:predict")
    suspend fun generateImage(
        @Path("model") modelName: String,
        @Query("key") apiKey: String,
        @Body request: ImageGenerationRequest
    ): GenerateContentResponse
}