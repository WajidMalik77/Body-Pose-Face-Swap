package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.io.IOException

class GeminiApiTester(private val apiKey: String) {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

    // Test 1: Check if API key is valid and get available models
    fun testApiKeyAndModels(callback: (Result<ApiTestResult>) -> Unit) {
        val url = "$baseUrl/models?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    try {
                        val modelsResponse = gson.fromJson(body, ModelsResponse::class.java)

                        // Check for specific Gemini models
                        val availableModels = modelsResponse.models?.map { it.name } ?: emptyList()
                        val hasGeminiPro = availableModels.any {
                            it.contains("gemini-pro") || it.contains("gemini-1.5")
                        }

                        val result = ApiTestResult(
                            isValid = true,
                            availableModels = availableModels,
                            hasGeminiPro = hasGeminiPro,
                            rawResponse = body
                        )

                        callback(Result.success(result))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                } else {
                    // Check if it's an authentication error
                    val isAuthError = response.code == 401 || response.code == 403
                    val errorMessage = "API Error: ${response.code}\n$body"

                    val result = ApiTestResult(
                        isValid = false,
                        errorCode = response.code,
                        errorMessage = errorMessage,
                        isAuthenticationError = isAuthError
                    )

                    callback(Result.success(result))
                }
            }
        })
    }

    // Test 2: Simple content generation test
    fun testContentGeneration(prompt: String, callback: (Result<ContentTestResult>) -> Unit) {
        val url = "$baseUrl/models/gemini-2.5-pro:generateContent?key=$apiKey"

        // Use gemini-1.5-pro-latest if available, otherwise fallback to gemini-pro
        val modelToTest = if (isGemini15Available()) {
            "gemini-2.5-pro"
        } else {
            "gemini-2.5-pro"
        }

        val testUrl = "$baseUrl/models/$modelToTest:generateContent?key=$apiKey"

        val requestBody = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 100
            )
        )

        val json = gson.toJson(requestBody)
        val mediaType = "application/json".toMediaType()

        val request = Request.Builder()
            .url(testUrl)
            .post(json.toRequestBody(mediaType))
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    try {
                        val contentResponse = gson.fromJson(body, GenerateContentResponse::class.java)
                        val text = contentResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: "No response text"

                        val result = ContentTestResult(
                            isSuccessful = true,
                            responseText = text,
                            modelUsed = modelToTest,
                            rawResponse = body
                        )

                        callback(Result.success(result))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                } else {
                    val result = ContentTestResult(
                        isSuccessful = false,
                        errorCode = response.code,
                        errorMessage = "Error ${response.code}: $body",
                        modelUsed = modelToTest
                    )

                    callback(Result.success(result))
                }
            }
        })
    }

    private fun isGemini15Available(): Boolean {
        // You can implement logic to check available models
        // For now, we'll assume it's available
        return true
    }

    // Data classes
    data class ApiTestResult(
        val isValid: Boolean,
        val availableModels: List<String> = emptyList(),
        val hasGeminiPro: Boolean = false,
        val errorCode: Int? = null,
        val errorMessage: String? = null,
        val isAuthenticationError: Boolean = false,
        val rawResponse: String? = null
    )

    data class ContentTestResult(
        val isSuccessful: Boolean,
        val responseText: String = "",
        val modelUsed: String,
        val errorCode: Int? = null,
        val errorMessage: String? = null,
        val rawResponse: String? = null
    )
}

// Request/Response data classes
data class ModelsResponse(
    val models: List<Model>?
)

data class Model(
    val name: String,
    val version: String,
    val displayName: String,
    val description: String,
    val inputTokenLimit: Int,
    val outputTokenLimit: Int,
    val supportedGenerationMethods: List<String>
)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Double,
    val maxOutputTokens: Int
)

data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content
)