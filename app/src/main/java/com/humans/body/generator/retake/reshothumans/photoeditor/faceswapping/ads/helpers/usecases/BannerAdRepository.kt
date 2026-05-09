package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.usecases

interface BannerAdRepository {
    fun getBannerVisibility(screen: String, position: String): Boolean
    fun getBannerType(screen: String, position: String): String
}