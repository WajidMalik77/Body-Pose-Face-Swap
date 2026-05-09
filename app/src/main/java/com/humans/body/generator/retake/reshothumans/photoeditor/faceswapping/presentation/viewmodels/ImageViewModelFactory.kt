package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.repositories.ImageRepository

class ImageViewModelFactory(
    private val repository: ImageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
