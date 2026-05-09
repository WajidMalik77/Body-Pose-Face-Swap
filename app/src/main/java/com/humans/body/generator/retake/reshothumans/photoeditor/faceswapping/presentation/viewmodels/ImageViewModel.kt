package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageResponse
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.ImageToImageResponse
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.repositories.ImageRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.MixedItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ImageViewModel(private val repository: ImageRepository) : ViewModel() {

init {
    Log.d("TAG", "ImageViewModel: created ")
}
    private val _data = MutableLiveData<List<MixedItem>>()
    val data: LiveData<List<MixedItem>> get() = _data

    fun loadData(context: Context) {
        if (_data.value?.isNotEmpty() == true) return

        viewModelScope.launch {
            _data.value = repository.loadMyData(context)
        }
    }
    fun generateImage(
        apiKey: String,
        prompt: String,
        onSuccess: (ImageResponse) -> Unit,
        onFailure: (String?) -> Unit
    ) {
        viewModelScope.launch {


            repository.generateImage(apiKey, prompt) { result ->
                result.onSuccess {
                    onSuccess.invoke(it)
//                    _imageResponse.postValue(it)
                }
                    .onFailure {
                        onFailure.invoke(it.message)
//                        _error.postValue(it.message)
                    }
            }
        }
    }


    fun generateImageToImage(
        apiKey: String,
        prompt: String,
        base64Image: String,
        onSuccess: (ImageToImageResponse) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("VM", "generateImageToImage called")

        repository.generateImageToImage(apiKey, prompt, base64Image) { result ->
            result.onSuccess {
                Log.d("VM", "Success received")
                onSuccess(it)
            }.onFailure {
                Log.e("VM", "Failure received: ${it.message}")
                onFailure(it.message ?: "Unknown error")
            }
        }
    }


    sealed class ImageProcessingState {
        object Idle : ImageProcessingState()
        object Loading : ImageProcessingState()
        data class Success(val bitmap: Bitmap) : ImageProcessingState()
        data class Error(val message: String) : ImageProcessingState()
    }



        private val _processingState = MutableStateFlow<ImageProcessingState>(ImageProcessingState.Idle)
        val processingState: StateFlow<ImageProcessingState> = _processingState.asStateFlow()

        fun removeBackground(context: Context, imageUri: Uri, apiKey: String) {
            viewModelScope.launch {
                _processingState.value = ImageProcessingState.Loading

                val result = repository.removeBackgroundWithUri(context, imageUri, apiKey)

                result.onSuccess { bitmap ->
                    _processingState.value = ImageProcessingState.Success(bitmap)
                }.onFailure { exception ->
                    _processingState.value = ImageProcessingState.Error(exception.message ?: "Unknown error")
                }
            }
        }

        fun removeBackground(bitmap: Bitmap, apiKey: String) {
            viewModelScope.launch {
                _processingState.value = ImageProcessingState.Loading

                val result = repository.removeBackgroundWithBitmap(bitmap, apiKey)

                result.onSuccess { processedBitmap ->
                    _processingState.value = ImageProcessingState.Success(processedBitmap)
                }.onFailure { exception ->
                    _processingState.value = ImageProcessingState.Error(exception.message ?: "Unknown error")
                }
            }
        }

        fun resetState() {
            _processingState.value = ImageProcessingState.Idle
        }
    }
    /*  fun generateImageToImage(
          apiKey: String,
          prompt: String,
          imageFile: String,
          onSuccess: (ImageToImageResponse) -> Unit,
          onFailure: (String?) -> Unit
      ) {
          Log.d("TAG", "handleImageUri: called vm")

          viewModelScope.launch {
              repository.generateImageToImage(apiKey, prompt, imageFile) { result ->
                  result.onSuccess {
                      Log.d("TAG", "handleImageUri: onSuccess")

                      onSuccess.invoke(it)
  //                    _imageResponse.postValue(it)
                  }
                      .onFailure {
                          Log.d("TAG", "handleImageUri: onFailure")

                          onFailure.invoke(it.message)
  //                        _error.postValue(it.message)
                      }
              }
          }
      }*/



