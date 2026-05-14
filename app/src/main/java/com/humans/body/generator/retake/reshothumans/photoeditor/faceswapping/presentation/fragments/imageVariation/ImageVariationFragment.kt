package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.imageVariation

import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.VariotionImageAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.VariotionTextAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentImageVariationBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.ResultHolder
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getVariationNumber
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageVariationFragment : Fragment() {

    private var _binding: FragmentImageVariationBinding? = null
    private val binding get() = _binding!!

    private val args: ImageVariationFragmentArgs by navArgs()

    private var count: Int = 0
    private var isCountSelected = false
    private var originalBitmap =
        android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageVariationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "ImageVariationFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "ImageVariationFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f

        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = getString(R.string.image_variation)

        attachFirstImage()

        binding.upload.setOnClickListener {
            findNavController().navigate(
                ImageVariationFragmentDirections.actionImageVariationFragmentToGalleryFragment(
                    featureType = FeatureType.IMAGE_VARIATION,
                    imageSlot = ImageSlot.FIRST
                )
            )
        }

        binding.recyclerText.adapter =
            VariotionTextAdapter(requireContext().getVariationNumber()) { pos ->
                count = pos
                isCountSelected = true
            }

        binding.generate.setOnClickListener {
            if (!isCountSelected) {
                Toast.makeText(
                    requireContext(),
                    "Please select variation number",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            var generationStarted = false
            runWithRewardedGate(
                screen = "ImageVariationFragmentScreen",
                trigger = "generate",
                onAdShowing = {
                    if (!generationStarted) {
                        generationStarted = true
                        performVariation()
                    }
                }
            ) {
                if (!generationStarted) {
                    generationStarted = true
                    performVariation()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun attachFirstImage() {
        val uri = args.selectedImageUri
        val resId = args.selectedResId
        when {
            uri.isNotEmpty() -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(Uri.parse(uri)).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }?.let { bmp ->
                        binding.generatedImage.setImageBitmap(bmp)
                        originalBitmap = bmp
                    }
                    enableGenerate()
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val d = requireContext().getDrawable(resId) ?: return@runCatching null
                            val bmp = createBitmap(
                                d.intrinsicWidth.coerceAtLeast(512),
                                d.intrinsicHeight.coerceAtLeast(512)
                            )
                            Canvas(bmp).also { c ->
                                d.setBounds(
                                    0,
                                    0,
                                    c.width,
                                    c.height
                                ); d.draw(c)
                            }
                            bmp
                        }.getOrNull()
                    }?.let { bmp ->
                        binding.generatedImage.setImageBitmap(bmp)
                        originalBitmap = bmp
                    }
                    enableGenerate()
                }
            }
        }
    }

    private fun enableGenerate() {
        if (!isAdded) return
        binding.generate.isEnabled = true
        binding.generate.alpha = 1f
    }

    // ── ONLY adapter click changed — each variation tap shows before/after ──

    private fun performVariation() {
        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f
        binding.loadingOverlay.visibility = View.VISIBLE

        val beforeBitmap = binding.generatedImage.drawable.toBitmap()

        GeminiImageService().generateImageVariations(
            SharePref.getString(Constants.new_version_key, ""),
            beforeBitmap, count
        ) { result ->
            if (!isAdded) return@generateImageVariations
            runWhenRewardedAdClosed {
                result.onSuccess { bitmaps ->
                    if (isAdded) {
                        binding.loadingOverlay.visibility = View.GONE
                        binding.recyclerText.visibility = View.GONE
                        binding.generate.isEnabled = true
                        binding.generate.alpha = 1f
                        binding.recyclerImage.adapter =
                            VariotionImageAdapter(bitmaps) { selectedVariation ->
                                // Before = original uploaded image, After = selected variation
                                ResultHolder.beforeBitmap = originalBitmap
                                ResultHolder.afterBitmap = selectedVariation
                                safeShowInterstitialNavigate("ImageVariationFragmentScreen", "select_variation") {
                                    findNavController().navigate(R.id.action_imageVariationFragment_to_beforeAfterFragment)
                                }
                            }
                    }
                }
                result.onFailure { error ->
                    if (isAdded) {
                        binding.loadingOverlay.visibility = View.GONE
                        binding.generate.isEnabled = true
                        binding.generate.alpha = 1f
                        Toast.makeText(
                            requireContext(),
                            "Failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
