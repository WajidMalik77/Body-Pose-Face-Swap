package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini


import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.graphics.BitmapFactory
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.Content
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GenerateContentResponse
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeneratedImage
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageGenerationConfig
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageGenerationRequest
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageGenerationResponse
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.Part
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.RetrofitClient1

import java.util.regex.Pattern

class GeminiRepository(private val apiKey: String) {

    private val apiService = RetrofitClient1.geminiApiService

    suspend fun generateImageFromText(
        prompt: String,
        modelName: String = "imagen-3.0-generate-001"
    ): ImageGenerationResponse {
        return withContext(Dispatchers.IO) {
            try {
                // Create enhanced prompt for image generation
                val enhancedPrompt = """
                    Generate a high-quality image based on this description:
                    
                    $prompt
                    
                    Requirements:
                    1. Create a detailed, visually appealing image
                    2. Output as base64 encoded PNG image
                    3. Image resolution: 1024x1024
                    4. Style: Photorealistic
                    5. Format: data:image/png;base64,[YOUR_BASE64_DATA]
                    
                    Important: Only return the base64 image data starting with "data:image/png;base64,"
                """.trimIndent()

                val request = ImageGenerationRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = enhancedPrompt)
                            )
                        )
                    ),
                    generationConfig = ImageGenerationConfig()
                )

                val response = apiService.generateImage(modelName, apiKey, request)

                // Parse response to extract image
                val images = extractImagesFromResponse(response)

                return@withContext ImageGenerationResponse(
                    success = images.isNotEmpty(),
                    images = images,
                    prompt = prompt
                )

            } catch (e: Exception) {
                return@withContext ImageGenerationResponse(
                    success = false,
                    errorMessage = e.message ?: "Unknown error occurred",
                    prompt = prompt
                )
            }
        }
    }

    private fun extractImagesFromResponse(response: GenerateContentResponse): List<GeneratedImage> {
        val images = mutableListOf<GeneratedImage>()

        response.candidates?.forEach { candidate ->
            candidate.content.parts.forEach { part ->
                part.text?.let { text ->
                    // Look for base64 image data in response
                    val base64Images = extractBase64Images(text)
                    base64Images.forEach { base64Data ->
                        val bitmap = decodeBase64ToBitmap(base64Data)
                        if (bitmap != null) {
                            images.add(
                                GeneratedImage(
                                    bitmap = bitmap,
                                    base64Data = base64Data,
                                    description = extractDescriptionFromText(text)
                                )
                            )
                        }
                    }
                }
            }
        }

        return images
    }

    private fun extractBase64Images(text: String): List<String> {
        val pattern = Pattern.compile("data:image/(png|jpeg|jpg);base64,([A-Za-z0-9+/=]+)")
        val matcher = pattern.matcher(text)
        val images = mutableListOf<String>()

        while (matcher.find()) {
            images.add(matcher.group())
        }

        // Also try to find raw base64 data
        if (images.isEmpty()) {
            val rawBase64Pattern = Pattern.compile("([A-Za-z0-9+/=]{100,})")
            val rawMatcher = rawBase64Pattern.matcher(text)
            if (rawMatcher.find()) {
                val potentialBase64 = rawMatcher.group()
                // Check if it looks like base64 image data
                if (potentialBase64.length > 100) {
                    images.add("data:image/png;base64,$potentialBase64")
                }
            }
        }

        return images
    }

    private fun decodeBase64ToBitmap(base64Data: String): Bitmap? {
        return try {
            val base64Pattern = Pattern.compile("data:image/[^;]+;base64,([A-Za-z0-9+/=]+)")
            val matcher = base64Pattern.matcher(base64Data)

            val cleanBase64 = if (matcher.find()) {
                matcher.group(1)
            } else {
                base64Data.replace("data:image/[^;]+;base64,", "")
            }

            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractDescriptionFromText(text: String): String {
        // Try to extract description from the response
        val lines = text.split("\n")
        for (line in lines) {
            if (line.contains("image", ignoreCase = true) &&
                !line.contains("base64", ignoreCase = true)) {
                return line.trim()
            }
        }
        return "Generated image"
    }

    suspend fun testApiKey(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAvailableModels(apiKey)
                response.models.isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }
}
