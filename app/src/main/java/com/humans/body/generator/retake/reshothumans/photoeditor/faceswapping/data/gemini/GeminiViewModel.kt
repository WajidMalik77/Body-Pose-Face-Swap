package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini


import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GeminiViewModel(
    private val repository: GeminiRepository
) : ViewModel() {

    private val _image = MutableLiveData<Bitmap>()
    val image: LiveData<Bitmap> = _image

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun generateTextToImage(apiKey: String, prompt: String) {
        viewModelScope.launch {
            try {
                _loading.postValue(true)
//                val bitmap = repository.textToImage(apiKey, prompt)
//                _image.postValue(bitmap)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun generateImageToImage(
        apiKey: String,
        prompt: String,
        bitmap: Bitmap
    ) {
        viewModelScope.launch {
            try {
                _loading.postValue(true)
//                val result = repository.imageToImage(apiKey, prompt, bitmap)
//                _image.postValue(result)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
