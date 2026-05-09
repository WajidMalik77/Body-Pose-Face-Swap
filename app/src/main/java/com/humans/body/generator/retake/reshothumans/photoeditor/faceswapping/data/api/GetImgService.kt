package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object GetImgService {

    suspend fun removeBackground(
        apiKey: String,
        bitmap: Bitmap
    ): Result<Bitmap> = withContext(Dispatchers.IO) {

        runCatching {
            val base64 = bitmapToBase64(bitmap)
            Log.d("GetImgService", "removeBackground → sending request, base64 size: ${base64.length}")

            val response = RetrofitClient.api.removeBackground(
                auth = "Bearer $apiKey",
                body = BgRemoveRequest(image = base64)
            )

            Log.d("GetImgService", "removeBackground → HTTP ${response.code()}")

            if (!response.isSuccessful) {
                val error = response.errorBody()?.string()
                Log.e("GetImgService", "removeBackground → error body: $error")
                throw Exception("getimg.ai error ${response.code()}: $error")
            }

            val imageB64 = response.body()?.image
                ?.takeIf { it.isNotEmpty() }
                ?: run {
                    Log.e("GetImgService", "removeBackground → no image in response body")
                    throw Exception("No image in response")
                }

            Log.d("GetImgService", "removeBackground → image received, base64 size: ${imageB64.length}")

            base64ToBitmap(imageB64)
                ?: run {
                    Log.e("GetImgService", "removeBackground → failed to decode bitmap")
                    throw Exception("Failed to decode result bitmap")
                }
        }.also { result ->
            result.onFailure { Log.e("GetImgService", "removeBackground → failed: ${it.message}") }
            result.onSuccess { Log.d("GetImgService", "removeBackground → success ✅") }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun base64ToBitmap(base64: String): Bitmap? = try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}