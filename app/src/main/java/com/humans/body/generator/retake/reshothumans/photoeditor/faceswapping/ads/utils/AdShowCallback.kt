package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.utils

interface AdShowCallback {
    fun onAdShown()
    fun onAdFailedToShow(adError: String)
    fun onAdDismissed()
}