package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini


sealed class ImageGenerationResult {
    data class Success(val text: String) : ImageGenerationResult()
    data class Error(val message: String) : ImageGenerationResult()
    object Loading : ImageGenerationResult()
}

sealed class ImageAnalysisResult {
    data class Success(val text: String) : ImageAnalysisResult()
    data class Error(val message: String) : ImageAnalysisResult()
    object Loading : ImageAnalysisResult()
}