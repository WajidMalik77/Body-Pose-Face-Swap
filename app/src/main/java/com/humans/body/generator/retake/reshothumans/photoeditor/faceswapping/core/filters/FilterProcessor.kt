package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

class FilterProcessor {

    fun applyFilter(bitmap: Bitmap, type: FilterType): Bitmap {
        return when (type) {
            FilterType.NONE -> bitmap
            FilterType.SEPIA -> applyColorMatrix(bitmap, sepiaMatrix())
            FilterType.BLACK_WHITE -> applyColorMatrix(bitmap, bwMatrix())
            FilterType.COOL -> applyColorMatrix(bitmap, coolMatrix())
            FilterType.WARM -> applyColorMatrix(bitmap, warmMatrix())
            FilterType.VINTAGE -> applyColorMatrix(bitmap, vintageMatrix())
            FilterType.BRIGHT -> applyColorMatrix(bitmap, brightMatrix())
            FilterType.CONTRAST -> applyColorMatrix(bitmap, contrastMatrix())
            FilterType.FADE -> applyColorMatrix(bitmap, fadeMatrix())
            FilterType.FILM -> applyColorMatrix(bitmap, filmMatrix())
            FilterType.SHARP -> applyColorMatrix(bitmap, sharpMatrix())
            FilterType.SOFT -> applyColorMatrix(bitmap, softMatrix())
            FilterType.NIGHT -> applyColorMatrix(bitmap, nightMatrix())
            FilterType.SUNSET -> applyColorMatrix(bitmap, sunsetMatrix())
            FilterType.PINK -> applyColorMatrix(bitmap, pinkMatrix())
            FilterType.GREEN -> applyColorMatrix(bitmap, greenMatrix())
            FilterType.BLUE -> applyColorMatrix(bitmap, blueMatrix())
            FilterType.CYAN -> applyColorMatrix(bitmap, cyanMatrix())

        }
    }
    private fun contrastMatrix(): ColorMatrix {
        val c = 1.5f
        val t = (-0.5f * c + 0.5f) * 255f
        return ColorMatrix(floatArrayOf(
            c, 0f, 0f, 0f, t,
            0f, c, 0f, 0f, t,
            0f, 0f, c, 0f, t,
            0f, 0f, 0f, 1f, 0f
        ))
    }
    private fun fadeMatrix() = ColorMatrix(floatArrayOf(
        0.9f, 0f, 0f, 0f, 30f,
        0f, 0.9f, 0f, 0f, 30f,
        0f, 0f, 0.9f, 0f, 30f,
        0f, 0f, 0f, 1f, 0f
    ))
    private fun filmMatrix() = ColorMatrix(floatArrayOf(
        1.1f, -0.1f, 0f, 0f, 0f,
        -0.1f, 1.1f, 0f, 0f, 0f,
        0f, 0f, 1.1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
    private fun sharpMatrix() = ColorMatrix(floatArrayOf(
        1.3f, -0.2f, 0f, 0f, 0f,
        -0.2f, 1.3f, 0f, 0f, 0f,
        0f, 0f, 1.3f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
    private fun softMatrix() = ColorMatrix(floatArrayOf(
        0.95f, 0.05f, 0f, 0f, 10f,
        0.05f, 0.95f, 0f, 0f, 10f,
        0f, 0f, 1f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    ))
    private fun nightMatrix() = ColorMatrix(floatArrayOf(
        0.8f, 0f, 0f, 0f, 0f,
        0f, 0.8f, 0f, 0f, 0f,
        0f, 0f, 1.3f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
    private fun sunsetMatrix() = ColorMatrix(floatArrayOf(
        1.3f, 0f, 0f, 0f, 20f,
        0f, 1.1f, 0f, 0f, 10f,
        0f, 0f, 0.9f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
    private fun pinkMatrix() = ColorMatrix(floatArrayOf(
        1.2f, 0f, 0.2f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0.2f, 0f, 1.2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))

    private fun greenMatrix() = ColorMatrix(floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1.2f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))

    private fun blueMatrix() = ColorMatrix(floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.3f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))

    private fun cyanMatrix() = ColorMatrix(floatArrayOf(
        1f, 0.1f, 0.1f, 0f, 0f,
        0.1f, 1f, 0.1f, 0f, 0f,
        0.1f, 0.1f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))


    private fun applyColorMatrix(src: Bitmap, matrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(
            src.width,
            src.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(result)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return result
    }

    // ---------------- MATRICES ----------------

    private fun sepiaMatrix() = ColorMatrix().apply {
        set(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        ))
    }

    private fun bwMatrix() = ColorMatrix().apply { setSaturation(0f) }

    private fun coolMatrix() = ColorMatrix(floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))

    private fun warmMatrix() = ColorMatrix(floatArrayOf(
        1.2f, 0f, 0f, 0f, 0f,
        0f, 1.1f, 0f, 0f, 0f,
        0f, 0f, 0.9f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))

    private fun vintageMatrix() = ColorMatrix(floatArrayOf(
        0.9f, 0.5f, 0.1f, 0f, 0f,
        0.3f, 0.8f, 0.1f, 0f, 0f,
        0.2f, 0.3f, 0.5f, 0f, 0f,
        0f,  0f,  0f,  1f, 0f
    ))

    private fun brightMatrix() = ColorMatrix(floatArrayOf(
        1.2f, 0f, 0f, 0f, 30f,
        0f, 1.2f, 0f, 0f, 30f,
        0f, 0f, 1.2f, 0f, 30f,
        0f, 0f, 0f, 1f, 0f
    ))
}
