package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import android.content.Context

interface ConnectivityChecker {
    fun isConnected(context: Context): Boolean
}