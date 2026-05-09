package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiImageService {

    companion object {
        private const val TAG = "GeminiImageService"
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-image-preview:generateContent"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    // ======================================================
    // HELPERS
    // ======================================================

    suspend fun bitmapToBase64(bitmap: Bitmap): String =
        withContext(Dispatchers.Default) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        }

    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "base64ToBitmap failed: ${e.message}")
            null
        }
    }

    // ✅ Builds request with responseModalities to force Gemini to return image
    private fun buildRequest(apiKey: String, bodyJson: JSONObject): Request {
        bodyJson.put("generationConfig", JSONObject().apply {
            put("responseModalities", JSONArray().apply {
                put("TEXT")
                put("IMAGE")
            })
        })
        return Request.Builder()
            .url("$ENDPOINT?key=$apiKey")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    // ✅ Parses image from response + full logging for debugging
    // TODO: Remove logs before production release
    private fun parseImageFromResponse(responseBody: String): Bitmap? {
        return try {
            val json = JSONObject(responseBody)

            // TODO: Remove in production
            Log.d(TAG, "Response: ${responseBody.take(1000)}")

            if (json.has("error")) {
                val error = json.optJSONObject("error")
                Log.e(TAG, "API Error ${error?.optInt("code")}: ${error?.optString("message")}")
                return null
            }

            val candidates = json.optJSONArray("candidates") ?: run {
                Log.e(TAG, "No candidates in response")
                return null
            }

            for (i in 0 until candidates.length()) {
                val candidate = candidates.getJSONObject(i)
                Log.d(TAG, "Candidate[$i] finishReason: ${candidate.optString("finishReason")}")

                val content = candidate.optJSONObject("content") ?: continue
                val parts = content.optJSONArray("parts") ?: continue

                for (j in 0 until parts.length()) {
                    val part = parts.getJSONObject(j)

                    // Log text parts — shows why image wasn't returned
                    // TODO: Remove in production
                    part.optString("text").takeIf { it.isNotEmpty() }?.let {
                        Log.d(TAG, "Text part[$j]: $it")
                    }

                    val inline = part.optJSONObject("inlineData") ?: continue
                    val base64 = inline.optString("data")
                    Log.d(TAG, "Image found — mimeType: ${inline.optString("mimeType")}, size: ${base64.length}")

                    val bitmap = base64ToBitmap(base64)
                    if (bitmap != null) return bitmap
                }
            }

            Log.e(TAG, "No image found in response")
            null
        } catch (e: Exception) {
            Log.e(TAG, "parseImageFromResponse exception: ${e.message}")
            null
        }
    }

    // ======================================================
    // TEXT TO IMAGE
    // ======================================================
    fun generateTextToImageWithGemini(
        apiKey: String,
        prompt: String,
        callback: (Result<Bitmap>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val effectivePrompt = """
                    Generate a high-quality photorealistic human image.
                    Prompt: $prompt
                    Output only image content. No text, watermark, frame, or logo.
                """.trimIndent()

                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", effectivePrompt))
                            })
                        })
                    })
                }

                Log.d(TAG, "textToImage → sending request")
                val request = buildRequest(apiKey, bodyJson)

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "textToImage onFailure: ${e.message}")
                        mainHandler.post { callback(Result.failure(e)) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            val body = it.body?.string() ?: ""
                            Log.d(TAG, "textToImage HTTP ${response.code}")
                            if (!response.isSuccessful) {
                                Log.e(TAG, "textToImage error: $body")
                                mainHandler.post { callback(Result.failure(Exception("HTTP ${response.code}"))) }
                                return
                            }
                            val bitmap = parseImageFromResponse(body)
                            mainHandler.post {
                                if (bitmap != null) callback(Result.success(bitmap))
                                else callback(Result.failure(Exception("No image returned")))
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "textToImage exception: ${e.message}")
                mainHandler.post { callback(Result.failure(e)) }
            }
        }
    }

    // ======================================================
    // FACE SWAP / CHANGE FACE
    // ======================================================
    fun changeFace(
        apiKey: String,
        targetImage: Bitmap,
        faceImage: Bitmap,
        prompt: String,
        callback: (Result<Bitmap>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val targetBase64 = bitmapToBase64(targetImage)
                val faceBase64 = bitmapToBase64(faceImage)

                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", targetBase64)
                                }))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", faceBase64)
                                }))
                            })
                        })
                    })
                }

                Log.d(TAG, "changeFace → sending request, prompt: $prompt")
                val request = buildRequest(apiKey, bodyJson)

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "changeFace onFailure: ${e.message}")
                        mainHandler.post { callback(Result.failure(e)) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            val body = it.body?.string() ?: ""
                            Log.d(TAG, "changeFace HTTP ${response.code}")
                            if (!response.isSuccessful) {
                                Log.e(TAG, "changeFace error: $body")
                                mainHandler.post { callback(Result.failure(Exception("HTTP ${response.code}"))) }
                                return
                            }
                            val bitmap = parseImageFromResponse(body)
                            mainHandler.post {
                                if (bitmap != null) callback(Result.success(bitmap))
                                else callback(Result.failure(Exception("NoImageReturned")))
                            }
                        }
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "changeFace exception: ${e.message}")
                mainHandler.post { callback(Result.failure(e)) }
            }
        }
    }

    // ======================================================
    // REMOVE BACKGROUND
    // ======================================================
    fun removeBackgroundWithGemini(
        apiKey: String,
        bitmap: Bitmap,
        prompt: String = "Remove the background completely and make it transparent",
        callback: (Result<Bitmap>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val base64 = bitmapToBase64(bitmap)

                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64)
                                }))
                            })
                        })
                    })
                }

                Log.d(TAG, "removeBackground → sending request")
                val request = buildRequest(apiKey, bodyJson)

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "removeBackground onFailure: ${e.message}")
                        mainHandler.post { callback(Result.failure(e)) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            val body = it.body?.string() ?: ""
                            Log.d(TAG, "removeBackground HTTP ${response.code}")
                            if (!response.isSuccessful) {
                                Log.e(TAG, "removeBackground error: $body")
                                mainHandler.post { callback(Result.failure(Exception("HTTP ${response.code}"))) }
                                return
                            }
                            val resultBitmap = parseImageFromResponse(body)
                            mainHandler.post {
                                if (resultBitmap != null) callback(Result.success(resultBitmap))
                                else callback(Result.failure(Exception("No image returned")))
                            }
                        }
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "removeBackground exception: ${e.message}")
                mainHandler.post { callback(Result.failure(e)) }
            }
        }
    }

    // ======================================================
    // UPSCALE IMAGE
    // ======================================================
    fun upscaleImageWithGemini(
        apiKey: String,
        bitmap: Bitmap,
        callback: (Result<Bitmap>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val base64 = bitmapToBase64(bitmap)

                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text",
                                    "Upscale this image to high resolution, enhance sharpness and details, preserve original colors and style, no blur, no artifacts"
                                ))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64)
                                }))
                            })
                        })
                    })
                }

                Log.d(TAG, "upscaleImage → sending request")
                val request = buildRequest(apiKey, bodyJson)

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "upscaleImage onFailure: ${e.message}")
                        mainHandler.post { callback(Result.failure(e)) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            val body = it.body?.string() ?: ""
                            Log.d(TAG, "upscaleImage HTTP ${response.code}")
                            if (!response.isSuccessful) {
                                Log.e(TAG, "upscaleImage error: $body")
                                mainHandler.post { callback(Result.failure(Exception("HTTP ${response.code}"))) }
                                return
                            }
                            val resultBitmap = parseImageFromResponse(body)
                            mainHandler.post {
                                if (resultBitmap != null) callback(Result.success(resultBitmap))
                                else callback(Result.failure(Exception("No upscaled image returned")))
                            }
                        }
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "upscaleImage exception: ${e.message}")
                mainHandler.post { callback(Result.failure(e)) }
            }
        }
    }

    // ======================================================
    // GENERATE IMAGE VARIATIONS
    // ======================================================
    fun generateImageVariations(
        apiKey: String,
        image: Bitmap,
        variationCount: Int,
        callback: (Result<List<Bitmap>>) -> Unit
    ) {
        val results = mutableListOf<Bitmap>()
        var completed = 0

        repeat(variationCount) { index ->
            generateSingleVariation(apiKey, image, index) { result ->
                synchronized(this) {
                    completed++
                    result.onSuccess { results.add(it) }
                    result.onFailure { Log.e(TAG, "Variation[$index] failed: ${it.message}") }
                    if (completed == variationCount) {
                        if (results.isNotEmpty()) callback(Result.success(results))
                        else callback(Result.failure(Exception("No variations generated")))
                    }
                }
            }
        }
    }

    private fun generateSingleVariation(
        apiKey: String,
        image: Bitmap,
        index: Int,
        callback: (Result<Bitmap>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val imageBase64 = bitmapToBase64(image)

                val poseVariations = listOf(
                    "face looking straight at the camera with neutral posture",
                    "head slightly tilted to the left, shoulders relaxed",
                    "head slightly tilted to the right, subtle smile",
                    "three-quarter view facing left, chin slightly raised",
                    "three-quarter view facing right, chin slightly lowered",
                    "very subtle forward lean, confident posture",
                    "very subtle backward lean, relaxed posture",
                    "shoulders angled slightly left, face toward camera",
                    "shoulders angled slightly right, face toward camera",
                    "soft natural pose with gentle head turn and calm expression"
                )

                val hint = poseVariations[index % poseVariations.size]
                val prompt = """
                    Generate a high-quality variation of the provided image.
                    Preserve the subject's identity and structure exactly.
                    Apply ONLY this change: $hint.
                    Do not change background, age, gender, or ethnicity.
                    No text, watermark, or distortion.
                """.trimIndent()

                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", imageBase64)
                                }))
                            })
                        })
                    })
                }

                Log.d(TAG, "variation[$index] → sending request")
                val request = buildRequest(apiKey, bodyJson)

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "variation[$index] onFailure: ${e.message}")
                        mainHandler.post { callback(Result.failure(e)) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            val body = it.body?.string() ?: ""
                            Log.d(TAG, "variation[$index] HTTP ${response.code}")
                            if (!response.isSuccessful) {
                                Log.e(TAG, "variation[$index] error: $body")
                                mainHandler.post { callback(Result.failure(Exception("HTTP ${response.code}"))) }
                                return
                            }
                            val bitmap = parseImageFromResponse(body)
                            mainHandler.post {
                                if (bitmap != null) callback(Result.success(bitmap))
                                else callback(Result.failure(Exception("No image returned")))
                            }
                        }
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "variation[$index] exception: ${e.message}")
                mainHandler.post { callback(Result.failure(e)) }
            }
        }
    }
}
