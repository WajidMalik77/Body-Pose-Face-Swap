package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models

sealed class AdResult {
    object Shown : AdResult()
    object NotShown : AdResult()
    data class Failed(val error: String) : AdResult()
}