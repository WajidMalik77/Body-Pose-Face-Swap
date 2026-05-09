package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.reStyle

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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentReStyleBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.ResultHolder
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReStyleFragment : Fragment() {

    private var _binding: FragmentReStyleBinding? = null
    private val binding get() = _binding!!

    private val args: ReStyleFragmentArgs by navArgs()

    private var bitmap1: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReStyleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "ReStyleFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "ReStyleFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f
        binding.done.isEnabled = false
        binding.done.alpha = 0.5f

        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = getString(R.string.re_styler)

        attachFirstImage()

        binding.upload.setOnClickListener {
            safeShowInterstitialNavigate("ReStyleFragmentScreen", "upload_first") {
                findNavController().navigate(
                    ReStyleFragmentDirections.actionReStyleFragmentToGalleryFragment(
                        featureType = FeatureType.IN_PAINTING,
                        imageSlot = ImageSlot.FIRST
                    )
                )
            }
        }

        // Done navigates directly to generatePictureFragment (user already saw before/after)
        binding.done.setOnClickListener {
            if (bitmap1 != null) {
                ResultHolder.beforeBitmap = binding.generatedImage.drawable.toBitmap()
                ResultHolder.afterBitmap = bitmap1
                safeShowInterstitialNavigate("ReStyleFragmentScreen", "done") {
                    findNavController().navigate(R.id.action_reStyleFragment_to_beforeAfterFragment)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.upload_image_first),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.generate.setOnClickListener {
            runWithRewardedGate("ReStyleFragmentScreen", "generate") {
                performReStyle()
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
                lifecycleScope.launch {
                    bitmap1 = withContext(Dispatchers.IO) {
                        runCatching {
                            requireContext().uriToBitmap(
                                Uri.parse(uri)
                            )
                        }.getOrNull()
                    }
                    bitmap1?.let { binding.generatedImage.setImageBitmap(it) }
                    enableGenerate()
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    bitmap1 = withContext(Dispatchers.IO) {
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
                    }
                    bitmap1?.let { binding.generatedImage.setImageBitmap(it) }
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

    // ── ONLY success block changed ─────────────────────────────

    private fun performReStyle() {
        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f
        binding.loadingOverlay.visibility = View.VISIBLE

        val beforeBitmap = imageViewToBitmap(binding.generatedImage)!!

        GeminiImageService().changeFace(
            apiKey = SharePref.getString(Constants.new_version_key, ""),
            targetImage = beforeBitmap,
            faceImage = BitmapFactory.decodeResource(
                requireContext().resources,
                R.drawable.ghibili
            ),
            prompt = "Re style the ghibli naturally",
        ) { result ->
            result.onSuccess { bitmap ->
                if (isAdded) {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.GenTxt.text = getString(R.string.done)
                    binding.generate.isEnabled = true
                    binding.generate.alpha = 1f
                    bitmap1 = bitmap
                    binding.generatedImage.setImageBitmap(bitmap)
                    binding.done.isEnabled = true
                    binding.done.alpha = 1f
                    // ── Navigate to before/after immediately ──
                    ResultHolder.beforeBitmap = beforeBitmap
                    ResultHolder.afterBitmap = bitmap
                    findNavController().navigate(R.id.action_reStyleFragment_to_beforeAfterFragment)
                }
            }.onFailure {
                if (isAdded) {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.generate.isEnabled = true
                    binding.generate.alpha = 1f
                }
                it.printStackTrace()
            }
        }
    }
}
