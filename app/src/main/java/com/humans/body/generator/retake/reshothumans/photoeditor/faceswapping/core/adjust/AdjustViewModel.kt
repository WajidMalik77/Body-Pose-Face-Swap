package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adjust

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.filters.FilterProcessor
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.filters.FilterType

class AdjustViewModel : ViewModel() {

    private val filterProcessor = FilterProcessor()
    private val processor = AdjustProcessor()

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> = _bitmap

    fun setImage(bitmap: Bitmap) {
        originalBitmap = bitmap

        if (_bitmap.value == null) {
            _bitmap.value = bitmap
        }
    }
    private var originalBitmap: Bitmap? = null
    fun applyFilter(type: FilterType) {
        val base = originalBitmap ?: return
        _bitmap.value = filterProcessor.applyFilter(base, type)
    }

    fun rotateRight() {
        _bitmap.value?.let {
            _bitmap.value = processor.rotate(it, 90f)
        }
    }

    fun flipH() {
        _bitmap.value?.let {
            _bitmap.value = processor.flipHorizontal(it)
        }
    }

    fun flipV() {
        _bitmap.value?.let {
            _bitmap.value = processor.flipVertical(it)
        }
    }
}