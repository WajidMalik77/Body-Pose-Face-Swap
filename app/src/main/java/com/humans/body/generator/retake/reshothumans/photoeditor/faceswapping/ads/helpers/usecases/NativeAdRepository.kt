package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models.NativeAdColorConfig

interface NativeAdRepository {
    fun getNativeVisibility(screen: String, position: String): Boolean
    fun shouldNativePreload(screen: String, position: String): Boolean
    fun getNativeAdSize(screen: String, position: String): Int
    fun getNativeAdColorConfig(screen: String, position: String): NativeAdColorConfig?
}