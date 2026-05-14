package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentTemplatesBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.navigateWithBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import timber.log.Timber

class TemplatesFragment : Fragment() {
    private var _binding: FragmentTemplatesBinding? = null
    private var generatedBitmap: Bitmap? = null
    private var faceImage: Bitmap? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplatesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "TemplatesFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "TemplatesFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        val uriString = arguments?.getString("BITMAP_BYTES")
        val uri = uriString?.toUri()
        uri?.let {
            val bitmap = BitmapFactory.decodeStream(
                requireContext().contentResolver.openInputStream(it)
            )
            generatedBitmap = bitmap
            binding.generatedImage1.setImageBitmap(bitmap)
        }

        binding.toolbar.titleTextView.text = requireContext().getString(R.string.templates)

        binding.toolbar.imageView.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.upload.setOnClickListener {
            checkAndPickImage()
        }

        binding.generate.setOnClickListener {
            if (generatedBitmap == null || faceImage == null) {
                Toast.makeText(requireContext(), "Upload Image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var generationStarted = false
            runWithRewardedGate(
                screen = "TemplatesFragmentScreen",
                trigger = "generate",
                onAdShowing = {
                    if (!generationStarted) {
                        generationStarted = true
                        handleImageUri()
                    }
                }
            ) {
                if (!generationStarted) {
                    generationStarted = true
                    handleImageUri()
                }
            }
        }
    }


    private fun handleImageUri() {
        binding.loadingOverlay.visibility = View.VISIBLE
        GeminiImageService().changeFace(
            apiKey = SharePref.getString(Constants.new_version_key, ""),
            targetImage = generatedBitmap!!,
            faceImage = faceImage!!,
            prompt = "Swap the face with given image naturally",
        ) { result ->
            if (!isAdded) return@changeFace
            runWhenRewardedAdClosed {
                result.onSuccess { bitmap ->
                    if (isAdded) {
                        binding.loadingOverlay.visibility = View.GONE
                        navigateWithBitmap(
                            bitmap,
                            R.id.action_templatesFragment_to_generatePictureFragment
                        )
                    }
                }.onFailure {
                    if (isAdded) {
                        binding.loadingOverlay.visibility = View.GONE
                        it.printStackTrace()
                    }
                }
            }
        }
    }

    private fun checkAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            )
                openGallery()
            else requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
                openGallery()
            else requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openGallery() {
        Timber.d("openGallery: setting isPickingFromGallery = true")
        pickImageLauncher.launch("image/*")
    }

    val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.add.visibility = View.GONE
                binding.generate.visibility = View.VISIBLE
                binding.generatedImage.setImageURI(it)
                faceImage = requireContext().uriToBitmap(it)
            }
        }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openGallery()
            else Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
