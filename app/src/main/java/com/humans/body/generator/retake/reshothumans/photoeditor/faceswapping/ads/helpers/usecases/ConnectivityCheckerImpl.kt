package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import android.content.Context
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils.Constants
import javax.inject.Inject

class ConnectivityCheckerImpl @Inject constructor() : ConnectivityChecker {
    override fun isConnected(context: Context): Boolean {
        return Constants.isInternetAvailable(context)
    }
}