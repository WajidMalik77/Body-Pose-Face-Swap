package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ImageGenerationViewModel : ViewModel() {

    private val repository = GeminiRepository("AIzaSyCAVVsPMMyCUhU8xIPFoJjIP-RglKMBpMY") // Get from secure storage

    // LiveData for UI updates
    private val _generationState = MutableLiveData<GenerationState>()
    val generationState: LiveData<GenerationState> = _generationState

    private val _generatedImages = MutableLiveData<List<Bitmap>>()
    val generatedImages: LiveData<List<Bitmap>> = _generatedImages

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun generateImageFromText(prompt: String) {
        if (prompt.isBlank()) {
            _errorMessage.value = "Please enter a prompt"
            return
        }

        _isLoading.value = true
        _generationState.value = GenerationState.LOADING

        viewModelScope.launch {
            val result = repository.generateImageFromText(prompt)

            _isLoading.value = false

            if (result.success) {
                val bitmaps = result.images?.mapNotNull { it.bitmap } ?: emptyList()
                _generatedImages.value = bitmaps
                _generationState.value = GenerationState.SUCCESS
            } else {
                _errorMessage.value = result.errorMessage ?: "Failed to generate image"
                _generationState.value = GenerationState.ERROR
            }
        }
    }

    fun clearImages() {
        _generatedImages.value = emptyList()
        _generationState.value = GenerationState.IDLE
    }

    fun testApiConnection() {
        _isLoading.value = true

        viewModelScope.launch {
            val isValid = repository.testApiKey()
            _isLoading.value = false

            if (isValid) {
                _generationState.value = GenerationState.API_VALID
            } else {
                _errorMessage.value = "Invalid API Key"
                _generationState.value = GenerationState.ERROR
            }
        }
    }

    enum class GenerationState {
        IDLE, LOADING, SUCCESS, ERROR, API_VALID
    }
}