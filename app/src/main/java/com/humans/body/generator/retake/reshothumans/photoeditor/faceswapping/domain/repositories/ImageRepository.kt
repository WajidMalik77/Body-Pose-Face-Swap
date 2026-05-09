package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.BgRemoveRequest
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageApi
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageRequest
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageResponse
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageToImageRequest
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageToImageResponse
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.MixedItem
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getHomeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class ImageRepository(private val api: ImageApi) {
    private val tag = "ImageRepository"

    suspend fun loadMyData(context: Context): List<MixedItem> {
        return context.getHomeData()
    }

    // ── Text to Image ──────────────────────────────────────────

    fun generateImage(
        apiKey: String,
        prompt: String,
        onResult: (Result<ImageResponse>) -> Unit
    ) {
        val request = ImageRequest(prompt = prompt)
        val primaryKey = apiKey.trim()
        val fallbackCandidates = listOf(
            SharePref.getString(Constants.api_key, "").trim(),
            SharePref.getString(Constants.new_version_key, "").trim(),
            Constants.getimg_api_key.trim()
        )
            .filter { it.isNotBlank() }
            .distinct()
            .filter { it != primaryKey }

        val keysToTry = buildList {
            if (primaryKey.isNotBlank()) add(primaryKey)
            addAll(fallbackCandidates)
        }.distinct()

        fun attempt(index: Int) {
            if (index >= keysToTry.size) {
                onResult(Result.failure(Throwable("Text-to-image failed for all available keys")))
                return
            }

            val key = keysToTry[index]
            val authHeader = "Bearer $key"
            Log.d(tag, "generateImage attempt=${index + 1}/${keysToTry.size} keyLen=${key.length}")

            api.generateImage(authHeader, request).enqueue(object : Callback<ImageResponse> {
                override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        onResult(Result.success(response.body()!!))
                        return
                    }

                    val errorText = try {
                        response.errorBody()?.string().orEmpty()
                    } catch (_: Exception) {
                        ""
                    }
                    Log.e(tag, "generateImage HTTP ${response.code()} body=$errorText")

                    if (response.code() == 401 && index + 1 < keysToTry.size) {
                        attempt(index + 1)
                    } else {
                        val message = when {
                            response.code() == 401 && errorText.contains("invalid_api_key", ignoreCase = true) ->
                                "Invalid API key (${response.code()})"
                            response.code() == 401 ->
                                "Unauthorized (${response.code()})"
                            else ->
                                "API Error ${response.code()}"
                        }
                        onResult(Result.failure(Throwable(message)))
                    }
                }

                override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                    onResult(Result.failure(t))
                }
            })
        }

        if (keysToTry.isEmpty()) {
            onResult(Result.failure(Throwable("No API key available for text-to-image")))
            return
        }

        attempt(0)
    }

    // ── Image to Image ─────────────────────────────────────────

    fun generateImageToImage(
        apiKey: String,
        prompt: String,
        base64Image: String,
        onResult: (Result<ImageToImageResponse>) -> Unit
    ) {
        Log.d("Repo", "generateImageToImage: called")

        val request = ImageToImageRequest(prompt = prompt, image = base64Image)

        api.imageToImage("Bearer $apiKey", request)
            .enqueue(object : Callback<ImageToImageResponse> {
                override fun onResponse(
                    call: Call<ImageToImageResponse>,
                    response: Response<ImageToImageResponse>
                ) {
                    if (!response.isSuccessful) {
                        val errorText = try { response.errorBody()?.string() } catch (e: Exception) { "Unable to read error body" }
                        Log.e("Repo", "API Error ${response.code()}: $errorText")
                        onResult(Result.failure(Exception("API Error ${response.code()}")))
                        return
                    }
                    val body = response.body() ?: run {
                        Log.e("Repo", "Empty response body")
                        onResult(Result.failure(Exception("Empty response body")))
                        return
                    }
                    val base64 = when {
                        !body.image.isNullOrEmpty()           -> body.image
                        body.images?.isNotEmpty() == true     -> body.images[0].image
                        else                                  -> null
                    }
                    if (base64 == null) {
                        Log.e("Repo", "No image data in response")
                        onResult(Result.failure(Exception("No image data returned")))
                        return
                    }
                    Log.d("Repo", "Image generated successfully (base64 length=${base64.length})")
                    onResult(Result.success(body))
                }
                override fun onFailure(call: Call<ImageToImageResponse>, t: Throwable) {
                    Log.e("Repo", "Network failure: ${t.message}", t)
                    onResult(Result.failure(t))
                }
            })
    }

    // ── Remove Background (from URI) — now uses base64 JSON ───

    suspend fun removeBackgroundWithUri(
        context: Context,
        imageUri: Uri,
        apiKey: String
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            // Read bytes from URI and encode to base64
            val bytes  = context.contentResolver.openInputStream(imageUri)?.readBytes()
                ?: throw Exception("Cannot read image from URI")
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val response = api.removeBackground(
                auth = "Bearer $apiKey",
                body = BgRemoveRequest(image = base64)
            )

            if (!response.isSuccessful) {
                throw Exception("Failed to remove background: ${response.code()}")
            }

            val imageB64 = response.body()?.image
                ?.takeIf { it.isNotEmpty() }
                ?: throw Exception("No image in response")

            val resultBytes = Base64.decode(imageB64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.size)
                ?: throw Exception("Failed to decode result bitmap")
        }
    }

    // ── Remove Background (from Bitmap) — now uses base64 JSON ─

    suspend fun removeBackgroundWithBitmap(
        bitmap: Bitmap,
        apiKey: String
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val base64 = bitmapToBase64(bitmap)

            val response = api.removeBackground(
                auth = "Bearer $apiKey",
                body = BgRemoveRequest(image = base64)
            )

            if (!response.isSuccessful) {
                throw Exception("Failed to remove background: ${response.code()}")
            }

            val imageB64 = response.body()?.image
                ?.takeIf { it.isNotEmpty() }
                ?: throw Exception("No image in response")

            val resultBytes = Base64.decode(imageB64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.size)
                ?: throw Exception("Failed to decode result bitmap")
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun base64ToBitmap(base64String: String): Bitmap {
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
