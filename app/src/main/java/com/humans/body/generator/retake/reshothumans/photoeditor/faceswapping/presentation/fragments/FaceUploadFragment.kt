package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentFaceUploadBinding
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

class FaceUploadFragment : Fragment() {
    private var _binding: FragmentFaceUploadBinding? = null
    private val binding get() = _binding!!

    private val args: FaceUploadFragmentArgs by navArgs()

    private var firstBitmap: Bitmap? = null  // user's photo
    private var secondBitmap: Bitmap? = null  // hairstyle reference photo

    // ── Lifecycle ──────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceUploadBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "FaceUploadFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "FaceUploadFragmentScreen",
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

    // ── Toolbar ────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.imageView.setOnClickListener {
            findNavController().popBackStack(
                R.id.homeFragment,
                false
            )
        }
        binding.toolbar.titleTextView.text = getString(R.string.swap_hairstyle)
    }

    // ── First image (Safe Args) ────────────────────────────────

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

    // ── Second image (savedStateHandle) ───────────────────────

    private fun observeSecondImage() {
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("secondImageUri")
            ?.observe(viewLifecycleOwner) { uri ->
                if (uri.isNullOrEmpty()) return@observe
                binding.addImg1.visibility = View.GONE
                binding.emptyBg1.visibility = View.GONE
                Glide.with(this).load(Uri.parse(uri)).centerCrop().into(binding.generatedImage1)
                lifecycleScope.launch {
                    secondBitmap = withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }
                    syncGenerateButton()
                }
            }
    }

    private fun syncGenerateButton() {
        if (!isAdded) return
        binding.GenTxt.visibility =
            if (firstBitmap != null && secondBitmap != null) View.VISIBLE else View.GONE
    }

    // ── Click listeners ────────────────────────────────────────

    private fun setupClickListeners() {

        // Upload slot 1 → re-pick user photo
        binding.upload.setOnClickListener {
            findNavController().navigate(
                FaceUploadFragmentDirections.actionFaceUploadFragmentToGalleryFragment(
                    featureType = FeatureType.SWAP_HAIRSTYLE,
                    imageSlot = ImageSlot.FIRST
                )
            )
        }

        // Upload slot 2 → pick hairstyle reference
        binding.upload1.setOnClickListener {
            findNavController().navigate(
                FaceUploadFragmentDirections.actionFaceUploadFragmentToGalleryFragment(
                    featureType = FeatureType.SWAP_HAIRSTYLE,
                    imageSlot = ImageSlot.SECOND
                )
            )
        }

        binding.GenTxt.setOnClickListener {
            when {
                firstBitmap == null -> toast("Please upload your photo first")
                secondBitmap == null -> toast("Please upload a hairstyle reference photo")
                else -> checkPremiumAndGenerate()
            }
        }
    }

    // ── Premium / trial gate ───────────────────────────────────

    private fun checkPremiumAndGenerate() {
        var generationStarted = false
        runWithRewardedGate(
            screen = "FaceUploadFragmentScreen",
            trigger = "generate",
            onAdShowing = {
                if (!generationStarted) {
                    generationStarted = true
                    generate()
                }
            }
        ) {
            if (!generationStarted) {
                generationStarted = true
                generate()
            }
        }
    }


    private fun generate() {
        val target = firstBitmap ?: return
        val hair = secondBitmap ?: return

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.GenTxt.visibility = View.GONE

        GeminiImageService().changeFace(
            apiKey = SharePref.Companion.getString(Constants.Companion.new_version_key, ""),
            targetImage = target,
            faceImage = hair,
            prompt = "Swap the hairstyle from the second image onto the person in the first image naturally. Keep the face, skin tone, and everything else exactly the same. Only change the hairstyle.",
        ) { result ->
            if (!isAdded) return@changeFace
            runWhenRewardedAdClosed {
                binding.loadingOverlay.visibility = View.GONE
                binding.GenTxt.visibility = View.VISIBLE
                result
                    .onSuccess { bitmap ->
                        ResultHolder.beforeBitmap = target
                        ResultHolder.afterBitmap = bitmap
                        findNavController().navigate(R.id.action_faceUploadFragment_to_beforeAfterFragment)
                    }
                    .onFailure { error ->
                        Log.e("FaceUploadFragment", "Generation failed", error)
                        toast("Generation failed. Please try again.")
                    }
            }
        }
    }


    private fun drawableToBitmap(resId: Int): Bitmap? {
        val d = requireContext().getDrawable(resId) ?: return null
        val bmp = Bitmap.createBitmap(
            d.intrinsicWidth.coerceAtLeast(512),
            d.intrinsicHeight.coerceAtLeast(512),
            Bitmap.Config.ARGB_8888
        )
        Canvas(bmp).also { c -> d.setBounds(0, 0, c.width, c.height); d.draw(c) }
        return bmp
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

}
