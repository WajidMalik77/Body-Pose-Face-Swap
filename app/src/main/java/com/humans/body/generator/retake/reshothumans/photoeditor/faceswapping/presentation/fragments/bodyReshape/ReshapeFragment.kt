package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.bodyReshape

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.graphics.createBitmap
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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentReshapeBinding
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

class ReshapeFragment : Fragment() {

    private var _binding: FragmentReshapeBinding? = null
    private val binding get() = _binding!!

    private val args: ReshapeFragmentArgs by navArgs()

    private var firstBitmap: Bitmap? = null
    private var secondBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReshapeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "ReshapeFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "ReshapeFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )
        setupToolbar()
        attachFirstImage()
        observeSecondImage()
        setupClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = getString(R.string.reshape_body)
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
                    firstBitmap = withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }
                    syncGenerateButton()
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    firstBitmap = withContext(Dispatchers.IO) {
                        runCatching { drawableToBitmap(resId) }.getOrNull()
                    }
                    syncGenerateButton()
                }
            }
        }
    }

    // ── Observe both URI and resId from GalleryFragment for the second slot ───
    private fun observeSecondImage() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle

        // URI path (gallery photo)
        handle?.getLiveData<String>("secondImageUri")
            ?.observe(viewLifecycleOwner) { uri ->
                if (uri.isNullOrEmpty()) return@observe
                loadSecondImage(uri = uri)
            }

        // ResId path (template or recent saved from a drawable)
        handle?.getLiveData<Int>("secondImageResId")
            ?.observe(viewLifecycleOwner) { resId ->
                if (resId == null || resId == -1) return@observe
                loadSecondImage(resId = resId)
            }
    }

    private fun loadSecondImage(uri: String? = null, resId: Int = -1) {
        if (!isAdded) return
        binding.addImg1.visibility = View.GONE
        binding.emptyBg1.visibility = View.GONE
        when {
            uri != null -> {
                Glide.with(this).load(Uri.parse(uri)).centerCrop().into(binding.generatedImage1)
                lifecycleScope.launch {
                    secondBitmap = withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }
                    syncGenerateButton()
                }
            }
            resId != -1 -> {
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage1)
                lifecycleScope.launch {
                    secondBitmap = withContext(Dispatchers.IO) {
                        runCatching { drawableToBitmap(resId) }.getOrNull()
                    }
                    syncGenerateButton()
                }
            }
        }
    }

    private fun syncGenerateButton() {
        if (!isAdded) return
        binding.GenTxt.visibility =
            if (firstBitmap != null && secondBitmap != null) View.VISIBLE else View.GONE
    }

    private fun setupClickListeners() {
        binding.upload.setOnClickListener {
            safeShowInterstitialNavigate("ReshapeFragmentScreen", "upload_first") {
                findNavController().navigate(
                    ReshapeFragmentDirections.actionReshapeFragmentToGalleryFragment(
                        FeatureType.BODY_RESHAPE,
                        ImageSlot.FIRST
                    )
                )
            }
        }
        binding.upload1.setOnClickListener {
            safeShowInterstitialNavigate("ReshapeFragmentScreen", "upload_second") {
                findNavController().navigate(
                    ReshapeFragmentDirections.actionReshapeFragmentToGalleryFragment(
                        FeatureType.BODY_RESHAPE,
                        ImageSlot.SECOND
                    )
                )
            }
        }
        binding.GenTxt.setOnClickListener {
            when {
                firstBitmap == null -> toast("Please upload your image first")
                secondBitmap == null -> toast("Please upload the model pose image")
                else -> checkPremiumAndGenerate()
            }
        }
    }

    private fun checkPremiumAndGenerate() {
        runWithRewardedGate("ReshapeFragmentScreen", "generate") {
            generate()
        }
    }

    private fun generate() {
        val target = firstBitmap ?: return
        val face = secondBitmap ?: return

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.GenTxt.visibility = View.GONE

        GeminiImageService().changeFace(
            apiKey = SharePref.Companion.getString(Constants.Companion.new_version_key, ""),
            targetImage = target,
            faceImage = face,
            prompt = "Reshape the body naturally while keeping the face unchanged",
        ) { result ->
            if (!isAdded) return@changeFace
            binding.loadingOverlay.visibility = View.GONE
            binding.GenTxt.visibility = View.VISIBLE
            result
                .onSuccess { bitmap ->
                    ResultHolder.beforeBitmap = target
                    ResultHolder.afterBitmap = bitmap
                    safeShowInterstitialNavigate("ReshapeFragmentScreen", "done") {
                        findNavController().navigate(R.id.action_reshapeFragment_to_beforeAfterFragment)
                    }
                }
                .onFailure { error ->
                    Log.e("ReshapeFragment", "Generation failed", error)
                    toast("Generation failed. Please try again.")
                }
        }
    }

    private fun drawableToBitmap(resId: Int): Bitmap? {
        val d = requireContext().getDrawable(resId) ?: return null
        val bmp =
            createBitmap(d.intrinsicWidth.coerceAtLeast(512), d.intrinsicHeight.coerceAtLeast(512))
        Canvas(bmp).also { c -> d.setBounds(0, 0, c.width, c.height); d.draw(c) }
        return bmp
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
