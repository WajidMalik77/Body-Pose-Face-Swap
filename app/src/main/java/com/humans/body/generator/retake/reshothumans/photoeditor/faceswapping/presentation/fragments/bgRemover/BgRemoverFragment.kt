package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.bgRemover

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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentBgRemoverBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.ResultHolder
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BgRemoverFragment : Fragment() {

    private var _binding: FragmentBgRemoverBinding? = null
    private val binding get() = _binding!!

    private val args: BgRemoverFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBgRemoverBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "BgRemoverFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "BgRemoverFragmentScreen",
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
        binding.toolbar.titleTextView.text = getString(R.string.bg_remover)

        attachFirstImage()

        binding.upload.setOnClickListener {
            safeShowInterstitialNavigate("BgRemoverFragmentScreen", "upload_first") {
                findNavController().navigate(
                    BgRemoverFragmentDirections.actionBgRemoverFragmentToGalleryFragment(
                        featureType = FeatureType.REMOVE_BG,
                        imageSlot = ImageSlot.FIRST
                    )
                )
            }
        }

        binding.generate.setOnClickListener {
            runWithRewardedGate("BgRemoverFragmentScreen", "generate") {
                performBgRemoval()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── First image ────────────────────────────────────────────

    private fun attachFirstImage() {
        val uri = args.selectedImageUri
        val resId = args.selectedResId

        when {
            uri.isNotEmpty() -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(Uri.parse(uri)).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }?.let { binding.generatedImage.setImageBitmap(it) }
                    enableGenerate()
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
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
                    }?.let { binding.generatedImage.setImageBitmap(it) }
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

    // ── BG Removal — before/after ──────────────────────────────

    private fun performBgRemoval() {
        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f
        binding.loadingOverlay.visibility = View.VISIBLE

        val beforeBitmap = binding.generatedImage.drawable.toBitmap()

        GeminiImageService().removeBackgroundWithGemini(
            SharePref.getString(Constants.new_version_key, ""), beforeBitmap
        ) { result ->
            if (!isAdded || !isVisible) return@removeBackgroundWithGemini
            viewLifecycleOwner.lifecycleScope.launch {
                binding.generate.isEnabled = true
                binding.generate.alpha = 1f
                binding.loadingOverlay.visibility = View.GONE

                result
                    .onSuccess { resultBitmap ->
                        ResultHolder.beforeBitmap = beforeBitmap
                        ResultHolder.afterBitmap = resultBitmap
                        safeShowInterstitialNavigate("BgRemoverFragmentScreen", "done") {
                            findNavController().navigate(R.id.action_bgRemoverFragment_to_beforeAfterFragment)
                        }
                    }
                    .onFailure { error ->
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
