package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.Manifest
import android.app.Activity
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.MyApplication
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentCropBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.navigateWithBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.toCacheUri
import com.yalantis.ucrop.UCrop
import java.io.File

class CropFragment : Fragment() {
    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!
    private var bitmap1: Bitmap? = null
    private var myUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "CropFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "CropFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f

        binding.toolbar.imageView.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.titleTextView.text = requireContext().getString(R.string.crop_un_crop)

        binding.upload.setOnClickListener {
            checkAndPickImage()
        }

        binding.generate.setOnClickListener {
            if (bitmap1 != null) {
                safeShowInterstitialNavigate("CropFragmentScreen", "continue") {
                    navigateWithBitmap(bitmap1!!, R.id.action_cropFragment_to_generatePictureFragment)
                }
            } else {
                Toast.makeText(requireContext(), "Upload image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startUCropFromBitmap(bitmap: Bitmap) {
        val sourceUri = bitmap.toCacheUri(requireContext())
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "crop_${System.currentTimeMillis()}.jpg")
        )
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
            setHideBottomControls(true)
            setShowCropGrid(true)
            setShowCropFrame(true)
        }
        cropLauncher.launch(UCrop.of(sourceUri, destinationUri).withOptions(options).getIntent(requireContext()))
    }

    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = UCrop.getOutput(result.data!!)
            uri?.let {
                val bitmap = requireContext().contentResolver.openInputStream(it)
                    ?.use { input -> BitmapFactory.decodeStream(input) }
                bitmap1 = bitmap
                binding.generatedImage.setImageBitmap(bitmap)
                binding.generate.isEnabled = true
                binding.generate.alpha = 1f
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(result.data!!)
            Toast.makeText(requireContext(), error?.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndPickImage() {
        MyApplication.isResume = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
                openGallery()
            else requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                openGallery()
            else requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openGallery() { pickImageLauncher.launch("image/*") }

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.upload.visibility = View.GONE
            binding.generatedImage.setImageURI(it)
            myUri = it
            startUCropFromBitmap(imageViewToBitmap(binding.generatedImage)!!)
        }
    }

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openGallery()
        else Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
