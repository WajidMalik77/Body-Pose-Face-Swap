package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.aiResizer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.AdjustAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentAiResizerBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.ResultHolder
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getRatioList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.toCacheUri
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AiResizerFragment : Fragment() {

    private var _binding: FragmentAiResizerBinding? = null
    private val binding get() = _binding!!

    private val args: AiResizerFragmentArgs by navArgs()

    private var bitmap1: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiResizerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "AiResizerFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "AiResizerFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility  = View.INVISIBLE
        }

        binding.generate.visibility = View.GONE

        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = getString(R.string.ai_resizer)

        attachFirstImage()

        binding.upload.setOnClickListener {
            findNavController().navigate(
                AiResizerFragmentDirections.actionAiResizerFragmentToGalleryFragment(
                    featureType = FeatureType.AI_RESIZING,
                    imageSlot   = ImageSlot.FIRST
                )
            )
        }

        binding.recyclerText.adapter = AdjustAdapter(requireContext().getRatioList()) { pos ->
            if (bitmap1 != null) {
                startUCropFromBitmap(imageViewToBitmap(binding.generatedImage)!!, pos)
            } else {
                Toast.makeText(requireContext(), getString(R.string.upload_image_first), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── First image ────────────────────────────────────────────

    private fun attachFirstImage() {
        val uri   = args.selectedImageUri
        val resId = args.selectedResId

        when {
            uri.isNotEmpty() -> {
                binding.addImg.visibility  = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(Uri.parse(uri)).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    bitmap1 = withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }
                    bitmap1?.let { binding.generatedImage.setImageBitmap(it) }
                }
            }
            resId != -1 -> {
                binding.addImg.visibility  = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    bitmap1 = withContext(Dispatchers.IO) {
                        runCatching {
                            val d = requireContext().getDrawable(resId) ?: return@runCatching null
                            val bmp = createBitmap(d.intrinsicWidth.coerceAtLeast(512), d.intrinsicHeight.coerceAtLeast(512))
                            Canvas(bmp).also { c -> d.setBounds(0, 0, c.width, c.height); d.draw(c) }
                            bmp
                        }.getOrNull()
                    }
                    bitmap1?.let { binding.generatedImage.setImageBitmap(it) }
                }
            }
        }
    }

    // ── UCrop ──────────────────────────────────────────────────

    fun startUCropFromBitmap(bitmap: Bitmap, pos: Int) {
        val sourceUri      = bitmap.toCacheUri(requireContext())
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "crop_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(false)
            setHideBottomControls(false)
            setShowCropGrid(true)
            setAspectRatioOptions(
                pos,
                AspectRatio("Original", 0f, 0f),
                AspectRatio("Square", 1f, 1f),
                AspectRatio("2:3", 2f, 3f),
                AspectRatio("3:4", 3f, 4f),
                AspectRatio("5:7", 5f, 7f),
                AspectRatio("9:16", 9f, 16f)
            )
        }
        cropLauncher.launch(UCrop.of(sourceUri, destinationUri).withOptions(options).getIntent(requireContext()))
    }

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = UCrop.getOutput(result.data!!)
                uri?.let {
                    val croppedBitmap = requireContext().contentResolver.openInputStream(it)
                        ?.use { input -> BitmapFactory.decodeStream(input) }

                    // ── Before/After ──────────────────────────
                    ResultHolder.beforeBitmap = bitmap1
                    ResultHolder.afterBitmap  = croppedBitmap

                    bitmap1 = croppedBitmap
                    binding.generatedImage.setImageBitmap(croppedBitmap)

                    runWithRewardedGate("AiResizerFragmentScreen", "generate") {
                        findNavController().navigate(R.id.action_aiResizerFragment_to_beforeAfterFragment)
                    }
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                Toast.makeText(requireContext(), UCrop.getError(result.data!!)?.message, Toast.LENGTH_LONG).show()
            }
        }
}
