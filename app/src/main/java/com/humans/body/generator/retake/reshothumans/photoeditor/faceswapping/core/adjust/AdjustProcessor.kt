package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adjust

import android.graphics.Bitmap
import android.graphics.Matrix

class AdjustProcessor {

    // 🔄 Rotate
    fun rotate(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degree)
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
    }

    // ↔ Flip Horizontal
    fun flipHorizontal(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            preScale(-1f, 1f)
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
    }

    // ↕ Flip Vertical
    fun flipVertical(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            preScale(1f, -1f)
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
    }

    // ✂ Crop
    fun crop(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap {
        return Bitmap.createBitmap(bitmap, x, y, width, height)
    }
}