package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.io.FileNotFoundException
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.widget.FrameLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.FirebaseDatabase
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentGeneratePictureBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.hasStoragePermission
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.saveBitmapToGallery
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.toCacheUri

class GeneratePictureFragment : Fragment() {
    private var _binding: FragmentGeneratePictureBinding? = null
    private val binding get() = _binding!!
    private var generatedBitmap: Bitmap? = null
    private var removeWatermark = false
    private var isPremiumUser = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratePictureBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "GeneratePictureFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "GeneratePictureFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        isPremiumUser = PrefUtil.isPremium(requireContext())
        if (isPremiumUser) {
            removeWatermark = true
            binding.removeWatermarkSwitch.visibility = View.GONE
            binding.cardView2.post {
                val screenHeight = requireActivity().window.decorView.height
                val params = binding.cardView2.layoutParams
                params.height = (screenHeight * 0.6).toInt()
                binding.cardView2.layoutParams = params
            }
        } else {
            binding.removeWatermarkSwitch.visibility = View.VISIBLE
        }

        checkIfUserIsSubscribed { }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        binding.watermarkText.visibility = if (removeWatermark) View.GONE else View.VISIBLE
        binding.watermarkText.text = requireContext().getString(R.string.human_body_generator)

        binding.textView2.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), PremiumActivity::class.java))
        }

        val uriString = arguments?.getString("BITMAP_BYTES")
        val uri = uriString?.let { Uri.parse(it) }
        val bitmap = uri?.let { safeDecodeBitmap(it) }
        if (bitmap == null) {
            Toast.makeText(requireContext(), "Image not found. Please generate again.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        generatedBitmap = bitmap
        binding.generatedImage.setImageBitmap(bitmap)

        // ── Save button: save in background + navigate to SavedFragment ─
        binding.saveBtn.setOnClickListener {
            saveInBackground()
            safeShowInterstitialNavigate("GeneratePictureFragmentScreen", "save") {
                navigateToSaved(uriString)
            }
        }

        // ── Bottom feature cards ───────────────────────────────────────
        binding.layoutCardAiReshape.setOnClickListener {
            val u = generatedBitmap?.toCacheUri(requireContext()) ?: return@setOnClickListener
            safeShowInterstitialNavigate("GeneratePictureFragmentScreen", "reshape") {
                findNavController().navigate(
                    GeneratePictureFragmentDirections
                        .actionGeneratePictureFragmentToReshapeFragment(
                            selectedImageUri = u.toString(),
                            selectedResId = -1
                        )
                )
            }
        }

        binding.layoutCardAiImageVariation.setOnClickListener {
            val u = generatedBitmap?.toCacheUri(requireContext()) ?: return@setOnClickListener
            safeShowInterstitialNavigate("GeneratePictureFragmentScreen", "image_variation") {
                findNavController().navigate(
                    GeneratePictureFragmentDirections
                        .actionGeneratePictureFragmentToImageVariationFragment(
                            selectedImageUri = u.toString(),
                            selectedResId = -1
                        )
                )
            }
        }

        binding.layoutCardAiSwapHairstyle.setOnClickListener {
            val u = generatedBitmap?.toCacheUri(requireContext()) ?: return@setOnClickListener
            safeShowInterstitialNavigate("GeneratePictureFragmentScreen", "face_upload") {
                findNavController().navigate(
                    GeneratePictureFragmentDirections
                        .actionGeneratePictureFragmentToFaceUploadFragment(
                            selectedImageUri = u.toString(),
                            selectedResId = -1
                        )
                )
            }
        }

        binding.removeWatermarkSwitch.setOnClickListener {
            safeShowInterstitialNavigate(
                "GeneratePictureFragmentScreen",
                "remove_watermark",
                noCounterNeeded = true
            ) {
                applyWatermarkRemoval()
            }
        }
    }

    // ── Save to gallery silently in the background ─────────────
    private fun saveInBackground() {
        val bitmapToSave = getBitmapForSaving() ?: return
        if (hasStoragePermission(requireContext())) {
            saveBitmapToGallery(requireContext(), bitmapToSave)
        } else {
            requestStoragePermissionAndSave()
        }
    }

    // ── Navigate to SavedFragment passing the image URI ────────
    private fun navigateToSaved(uriString: String?) {
        val bundle = Bundle().apply {
            putString("IMAGE_URI", uriString)
        }
        findNavController().navigate(R.id.action_generatePictureFragment_to_savedFragment, bundle)
    }

    private fun applyWatermarkRemoval() {
        if (isPremiumUser) return
        removeWatermark = !removeWatermark
        binding.removeWatermarkSwitch.visibility = View.GONE
        generatedBitmap?.let { originalBitmap ->
            val watermarkText = if (removeWatermark) "" else "Human Body Generator"
            val updatedBitmap = addWatermarkToBitmap(requireContext(), originalBitmap, watermarkText)
            binding.generatedImage.setImageBitmap(updatedBitmap)
            binding.watermarkText.visibility = if (removeWatermark) View.GONE else View.VISIBLE
        } ?: Toast.makeText(requireContext(), "Image not ready yet", Toast.LENGTH_SHORT).show()
    }

    private fun requestStoragePermissionAndSave() {
        val bitmapToSave = getBitmapForSaving() ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                saveBitmapToGallery(requireContext(), bitmapToSave)
            } else {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            saveBitmapToGallery(requireContext(), bitmapToSave)
        }
    }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                getBitmapForSaving()?.let { saveBitmapToGallery(requireContext(), it) }
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun getBitmapForSaving(): Bitmap? {
        val bitmap = generatedBitmap ?: return null
        val shouldRemoveWatermark = isPremiumUser || removeWatermark
        val watermarkText = if (shouldRemoveWatermark) "" else "Human Body Generator"
        return addWatermarkToBitmap(requireContext(), bitmap, watermarkText)
    }

    private fun addWatermarkToBitmap(
        context: Context,
        originalBitmap: Bitmap,
        watermarkText: String
    ): Bitmap {
        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.Light)
            textSize = (originalBitmap.width * 0.05).toFloat()
            alpha = 102
            isAntiAlias = true
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val centerX = result.width / 2f
        val centerY = result.height / 2f
        canvas.withRotation(-45f, centerX, centerY) {
            drawText(watermarkText, centerX, centerY, paint)
        }
        return result
    }

    private fun checkIfUserIsSubscribed(callback: (Boolean) -> Unit) {
        val deviceId =
            Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        FirebaseDatabase.getInstance()
            .getReference("subscriptions")
            .child(deviceId)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.child("isSubscribed").getValue(Boolean::class.java) == true)
            }
            .addOnFailureListener { callback(false) }
    }

    private fun safeDecodeBitmap(uri: Uri): Bitmap? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (_: FileNotFoundException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }
}
