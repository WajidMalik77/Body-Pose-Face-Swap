package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

// Models Response
data class ModelsResponse(
    @SerializedName("models") val models: List<Model>
)

data class Model(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String,
    @SerializedName("inputTokenLimit") val inputTokenLimit: Int,
    @SerializedName("outputTokenLimit") val outputTokenLimit: Int,
    @SerializedName("supportedGenerationMethods") val supportedGenerationMethods: List<String>
)

// Request Classes
data class GenerateContentRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig? = null,
    @SerializedName("safetySettings") val safetySettings: List<SafetySetting>? = null
)

data class ImageGenerationRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("generationConfig") val generationConfig: ImageGenerationConfig
)

data class Content(
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String? = null,
    @SerializedName("inlineData") val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("data") val data: String
)

data class GenerationConfig(
    @SerializedName("temperature") val temperature: Double? = 0.7,
    @SerializedName("topP") val topP: Double? = 0.8,
    @SerializedName("topK") val topK: Int? = 40,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int? = 2048
)

data class ImageGenerationConfig(
    @SerializedName("temperature") val temperature: Double = 0.4,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 4096,
    @SerializedName("responseMimeType") val responseMimeType: String = "text/plain"
)

data class SafetySetting(
    @SerializedName("category") val category: String,
    @SerializedName("threshold") val threshold: String
)

// Response Classes
data class GenerateContentResponse(
    @SerializedName("candidates") val candidates: List<Candidate>?,
    @SerializedName("promptFeedback") val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    @SerializedName("content") val content: Content,
    @SerializedName("finishReason") val finishReason: String? = null,
    @SerializedName("safetyRatings") val safetyRatings: List<SafetyRating>? = null
)

data class SafetyRating(
    @SerializedName("category") val category: String,
    @SerializedName("probability") val probability: String
)

data class PromptFeedback(
    @SerializedName("safetyRatings") val safetyRatings: List<SafetyRating>? = null
)

// Image Generation Specific
data class ImageGenerationResponse(
    val success: Boolean,
    val images: List<GeneratedImage>? = null,
    val errorMessage: String? = null,
    val prompt: String? = null
)

data class GeneratedImage(
    val bitmap: Bitmap? = null,
    val base64Data: String = "",
    val description: String = ""
)