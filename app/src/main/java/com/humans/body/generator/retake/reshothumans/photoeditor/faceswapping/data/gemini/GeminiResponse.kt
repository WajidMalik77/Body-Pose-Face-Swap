package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini

//
//data class GeminiResponse(
//    @SerializedName("candidates") val candidates: List<Candidate>?,
//    @SerializedName("promptFeedback") val promptFeedback: PromptFeedback?
//)
//
//data class Candidate(
//    @SerializedName("content") val content: Content?,
//    @SerializedName("finishReason") val finishReason: String?,
//    @SerializedName("safetyRatings") val safetyRatings: List<SafetyRating>?
//)
//
//data class Content(
//    @SerializedName("parts") val parts: List<Part>?,
//    @SerializedName("role") val role: String?
//)
//
//data class Part(
//    @SerializedName("text") val text: String?,
//    @SerializedName("inlineData") val inlineData: InlineData?
//)
//
//data class InlineData(
//    @SerializedName("mimeType") val mimeType: String?,
//    @SerializedName("data") val data: String?
//)
//
//data class PromptFeedback(
//    @SerializedName("safetyRatings") val safetyRatings: List<SafetyRating>?
//)
//
//data class SafetyRating(
//    @SerializedName("category") val category: String?,
//    @SerializedName("probability") val probability: String?
//)
//
//// For image generation response
//data class ImageGenerationResponse(
//    @SerializedName("images") val images: List<GeneratedImage>?
//)
//
//data class GeneratedImage(
//    @SerializedName("bytesBase64Encoded") val bytesBase64Encoded: String?,
//    @SerializedName("mimeType") val mimeType: String?
//)
//
//
//data class GeminiTextRequest(
//    @SerializedName("contents") val contents: List<ContentItem>
//)
//
//data class GeminiImageRequest(
//    @SerializedName("contents") val contents: List<ContentItem>
//)
//
//data class ContentItem(
//    @SerializedName("parts") val parts: List<Part>
//)
//
//
//
//
//
//
//



//data class Part(
//    @SerializedName("text") val text: String? = null,
//    @SerializedName("inline_data") val inlineData: InlineData? = null
//)
//
//data class InlineData(
//    @SerializedName("mime_type") val mimeType: String,
//    @SerializedName("data") val data: String // base64 encoded image
//)