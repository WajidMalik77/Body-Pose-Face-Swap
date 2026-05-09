package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImageViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeminiViewModel(GeminiRepository("")) as T
    }
}
