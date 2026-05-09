package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
enum class FeatureType : Parcelable {
    BODY_RESHAPE,
    POSE_SELECTION,
    TEXT_TO_IMAGE,
    FACE_SWAP,
    FACE_STYLE,
    IMAGE_EDITING,
    IN_PAINTING,
    UPSCALING,
    AI_RESIZING,
    REMOVE_BG,
    IMAGE_VARIATION,
    SWAP_HAIRSTYLE
}
