package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.faceSwap

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
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentFaceSwapBinding
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

class FaceSwapFragment : Fragment() {

    private var _binding: FragmentFaceSwapBinding? = null
    private val binding get() = _binding!!

    private val args: FaceSwapFragmentArgs by navArgs()

    private var firstBitmap: Bitmap? = null
    private var secondBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceSwapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "FaceSwapFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "FaceSwapFragmentScreen",
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
        binding.toolbar.titleTextView.text = getString(R.string.face_swap)
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
                        runCatching {
                            requireContext().uriToBitmap(
                                Uri.parse(uri)
                            )
                        }.getOrNull()
                    }
                    syncGenerateButton()
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    firstBitmap =
                        withContext(Dispatchers.IO) { runCatching { drawableToBitmap(resId) }.getOrNull() }
                    syncGenerateButton()
                }
            }
        }
    }

    private fun observeSecondImage() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<String>("secondImageUri")
            ?.observe(viewLifecycleOwner) { uri ->
                if (uri.isNullOrEmpty()) return@observe
                binding.addImg1.visibility = View.GONE
                binding.emptyBg1.visibility = View.GONE
                Glide.with(this).load(Uri.parse(uri)).centerCrop().into(binding.generatedImage1)
                lifecycleScope.launch {
                    secondBitmap = withContext(Dispatchers.IO) {
                        runCatching {
                            requireContext().uriToBitmap(
                                Uri.parse(uri)
                            )
                        }.getOrNull()
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

    private fun setupClickListeners() {
        binding.upload.setOnClickListener {
            safeShowInterstitialNavigate("FaceSwapFragmentScreen", "upload_first") {
                findNavController().navigate(
                    FaceSwapFragmentDirections.actionFaceSwapFragmentToGalleryFragment(
                        FeatureType.FACE_SWAP,
                        ImageSlot.FIRST
                    )
                )
            }
        }
        binding.upload1.setOnClickListener {
            safeShowInterstitialNavigate("FaceSwapFragmentScreen", "upload_second") {
                findNavController().navigate(
                    FaceSwapFragmentDirections.actionFaceSwapFragmentToGalleryFragment(
                        FeatureType.FACE_SWAP,
                        ImageSlot.SECOND
                    )
                )
            }
        }
        binding.GenTxt.setOnClickListener {
            when {
                firstBitmap == null -> toast("Please upload your image first")
                secondBitmap == null -> toast("Please upload the target face image")
                else -> checkPremiumAndGenerate()
            }
        }
    }

    private fun checkPremiumAndGenerate() {
        runWithRewardedGate("FaceSwapFragmentScreen", "generate") {
            generate()
        }
    }

    // ── ONLY success block changed ─────────────────────────────

    private fun generate() {
        val target = firstBitmap ?: return
        val face = secondBitmap ?: return

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.GenTxt.visibility = View.GONE

        GeminiImageService().changeFace(
            apiKey = SharePref.getString(Constants.new_version_key, ""),
            targetImage = target,
            faceImage = face,
            prompt = "Swap the face naturally",
        ) { result ->
            if (!isAdded) return@changeFace
            binding.loadingOverlay.visibility = View.GONE
            binding.GenTxt.visibility = View.VISIBLE
            result
                .onSuccess { bitmap ->
                    ResultHolder.beforeBitmap = target
                    ResultHolder.afterBitmap = bitmap
                    findNavController().navigate(R.id.action_faceSwapFragment_to_beforeAfterFragment)
                }
                .onFailure { error ->
                    Log.e("FaceSwapFragment", "Generation failed", error)
                    toast("Generation failed. Please try again.")
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
