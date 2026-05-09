package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.graphics.Bitmap

object ResultHolder {
    var beforeBitmap: Bitmap? = null
    var afterBitmap: Bitmap? = null

    fun clear() {
        beforeBitmap = null
        afterBitmap  = null
    }
}