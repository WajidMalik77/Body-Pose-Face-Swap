package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.models

import android.widget.FrameLayout
import com.google.android.gms.ads.LoadAdError
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.config.models.NativeAdColorConfig

data class AdLoadParams(
    val adContainer: FrameLayout,
    val shimmerContainer: FrameLayout?,
    val adUnitId: String,
    val layoutRes: Int,
    val shimmerLayoutRes: Int,
    val colorConfig: NativeAdColorConfig?,
    val shouldPreloadNext: Boolean = true,
    val onImpression: (() -> Unit)? = null,
    val onLoaded: (() -> Unit)?,
    val onFailed: ((LoadAdError) -> Unit)?
)
