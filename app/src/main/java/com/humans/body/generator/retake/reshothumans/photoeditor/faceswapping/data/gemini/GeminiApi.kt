package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini


import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {

    @POST("v1beta/models/gemini-3-pro-image-preview:generateContent")
    suspend fun generateImage(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): GeminiResponse
}
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ContentResponse?
)

data class ContentResponse(
    val parts: List<PartResponse>?
)

data class PartResponse(
    val inlineData: InlineDataResponse?
)

data class InlineDataResponse(
    val data: String?
)
