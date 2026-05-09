package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.AdapterPose
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.EditorListAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleAdult
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleChild
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleTeen
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleAdult
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleChild
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleTeen
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomPoseBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentImageToImageBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getImageToImageList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.navigateWithBitmap
import android.widget.FrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageToImageFragment : Fragment() {
    private var _binding: FragmentImageToImageBinding? = null
    private val binding get() = _binding!!
    private var bitmap1: Bitmap? = null
    private var myUri: Uri? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var selectedChip: TextView? = null
    private var ageText = ""
    private var genderText = "Female"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageToImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "ImageToImageFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "ImageToImageFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = requireContext().getString(R.string.image_to_image)
        binding.upload.setOnClickListener { checkAndPickImage() }
        binding.done.setOnClickListener {
            if (bitmap1 != null) safeShowInterstitialNavigate("ImageToImageFragmentScreen", "done") {
                navigateWithBitmap(
                    bitmap1!!,
                    R.id.action_imageToImageFragment_to_generatePictureFragment
                )
            }
            else Toast.makeText(
                requireContext(),
                getString(R.string.upload_image_first),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.recycler.adapter =
            EditorListAdapter(requireContext().getImageToImageList()) { pos, name ->
                when (pos) {
                    0 -> showPoseBottomSheet(name)

                    1 -> {
                        runWithRewardedGate("ImageToImageFragmentScreen", "bg_removal") { performBgRemoval() }
                    }

                    2 -> {
                        runWithRewardedGate("ImageToImageFragmentScreen", "ghibli") { performGhibli() }
                    }

                    3 -> {
                        runWithRewardedGate("ImageToImageFragmentScreen", "upscale") { performUpscale() }
                    }
                }
            }
    }


    private fun performBgRemoval() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val bitmap = binding.generatedImage.drawable.toBitmap()
        GeminiImageService().removeBackgroundWithGemini(
            SharePref.getString(
                Constants.new_version_key,
                ""
            ), bitmap
        ) { result ->
            requireActivity().runOnUiThread {
                result.onSuccess { bmp ->
                    if (!isAdded) return@runOnUiThread
                    binding.loadingOverlay.visibility = View.GONE
                    bitmap1 = bmp
                    binding.generatedImage.setImageBitmap(bmp)
                }
                result.onFailure { error ->
                    if (!isAdded) return@runOnUiThread
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed: ${error.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun performGhibli() {
        binding.loadingOverlay.visibility = View.VISIBLE
        GeminiImageService().changeFace(
            apiKey = SharePref.getString(Constants.new_version_key, ""),
            targetImage = imageViewToBitmap(binding.generatedImage)!!,
            faceImage = BitmapFactory.decodeResource(
                requireContext().resources,
                R.drawable.ghibili
            ),
            prompt = "Re style the ghibli naturally",
        ) { result ->
            result.onSuccess { bmp ->
                if (isAdded) {
                    binding.loadingOverlay.visibility = View.GONE
                    bitmap1 = bmp
                    binding.generatedImage.setImageBitmap(bmp)
                }
            }.onFailure {
                if (isAdded) {
                    binding.loadingOverlay.visibility = View.GONE
                    it.printStackTrace()
                }
            }
        }
    }

    private fun performUpscale() {
        binding.loadingOverlay.visibility = View.VISIBLE
        GeminiImageService().upscaleImageWithGemini(
            SharePref.getString(Constants.new_version_key, ""),
            binding.generatedImage.drawable.toBitmap()
        ) { result ->
            requireActivity().runOnUiThread {
                result.onSuccess { upscaled ->
                    if (isAdded) {
                        binding.loadingOverlay.visibility = View.GONE
                        bitmap1 = upscaled
                        binding.generatedImage.setImageBitmap(upscaled)
                    }
                }
                result.onFailure {
                    if (isAdded) {
                        binding.loadingOverlay.visibility = View.GONE
                        Toast.makeText(requireContext(), "Upscale failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun showPoseBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val bottomSheetBinding = BottomPoseBinding.inflate(layoutInflater)
        bottomSheetDialog =
            BottomSheetDialog(requireContext()).apply { setContentView(bottomSheetBinding.root) }
        bottomSheetBinding.title.text = title
        bottomSheetBinding.recycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 4); setHasFixedSize(true)
        }

        val adapter = AdapterPose(getMaleTeen()) { faceImage ->
            bottomSheetDialog?.dismiss()
            runWithRewardedGate("ImageToImageFragmentScreen", "pose_change") { performPoseChange(faceImage) }
        }

        bottomSheetBinding.recycler.adapter = adapter
        addGenderOptions(listOf("Female", "Male"), bottomSheetBinding.genderContainer, adapter)
        listOf("Adult", "Teenage", "Child").forEach {
            bottomSheetBinding.ageContainer.addView(
                createAgeChip(it, adapter)
            )
        }

        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }


    private fun performPoseChange(faceImage: ImageView) {
        binding.loadingOverlay.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val targetBitmap =
                withContext(Dispatchers.Default) { imageViewToBitmap(binding.generatedImage) }
            val faceBitmap = withContext(Dispatchers.Default) { imageViewToBitmap(faceImage) }
            if (targetBitmap == null || faceBitmap == null) {
                binding.loadingOverlay.visibility = View.GONE; return@launch
            }
            GeminiImageService().changeFace(
                apiKey = SharePref.getString(Constants.new_version_key, ""),
                targetImage = targetBitmap, faceImage = faceBitmap,
                prompt = "Replace the pose naturally while preserving identity."
            ) { result ->
                if (!isAdded || !isVisible) return@changeFace
                viewLifecycleOwner.lifecycleScope.launch {
                    binding.loadingOverlay.visibility = View.GONE
                    result.onSuccess { bmp ->
                        bitmap1 = bmp; binding.generatedImage.setImageBitmap(
                        bmp
                    )
                    }
                        .onFailure { it.printStackTrace() }
                }
            }
        }
    }

    private fun createAgeChip(text: String, optionAdapter: AdapterPose): TextView {
        val context = requireContext()
        return TextView(context).apply {
            this.text = text; setTextColor(Color.WHITE); textSize = 14f; gravity =
            Gravity.CENTER; setPadding(18, 15, 18, 15)
            background = ContextCompat.getDrawable(context, R.drawable.custom_text_view)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 16, 16, 0) }
            setOnClickListener {
                selectedChip?.background =
                    ContextCompat.getDrawable(context, R.drawable.custom_text_view)
                background =
                    ContextCompat.getDrawable(context, R.drawable.custom_selected_text_view)
                selectedChip = this; ageText = text
                updateRecyclerData(optionAdapter, genderText, ageText)
            }
        }
    }

    private fun addGenderOptions(
        options: List<String>,
        container: LinearLayout,
        optionAdapter: AdapterPose
    ) {
        options.forEach { option ->
            val isSelected = option == "Female"
            val optionView = createGenderOption(option, isSelected) { selectedView ->
                val selectedText = selectedView.tag as String
                genderText = selectedText
                updateRecyclerData(optionAdapter, genderText, ageText)
                for (i in 0 until container.childCount) container.getChildAt(i)
                    .findViewById<ImageView>(R.id.checkIcon)
                    ?.setImageResource(R.drawable.ic_uncheck)
                selectedView.findViewById<ImageView>(R.id.checkIcon)
                    ?.setImageResource(R.drawable.ic_check)
            }
            container.addView(optionView)
        }
    }

    private fun updateRecyclerData(adapter: AdapterPose, gender: String = "Female", age: String?) {
        Log.d("TAG", "updateRecyclerData: $gender, $age")
        val data = when {
            gender == "Male" && age == "Teenage" -> getMaleTeen()
            gender == "Male" && age == "Adult" -> getMaleAdult()
            gender == "Male" && age == "Child" -> getMaleChild()
            gender == "Female" && age == "Teenage" -> getFemaleTeen()
            gender == "Female" && age == "Adult" -> getFemaleAdult()
            gender == "Female" && age == "Child" -> getFemaleChild()
            else -> if (gender == "Female") getFemaleAdult() else getMaleAdult()
        }
        adapter.updateData(data)
    }

    private fun createGenderOption(
        gender: String,
        isSelected: Boolean = false,
        onSelected: (View) -> Unit
    ): View {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_gender_option, null)
        view.findViewById<TextView>(R.id.label).text = gender
        view.findViewById<ImageView>(R.id.checkIcon)
            .setImageResource(if (isSelected) R.drawable.ic_check else R.drawable.ic_uncheck)
        view.tag = gender
        view.setOnClickListener { (it.tag as? String)?.let { _ -> onSelected(it) } }
        return view
    }

    private fun checkAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) openGallery()
            else requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) openGallery()
            else requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openGallery() {
        Log.d("GALLERY", "openGallery: setting isPickingFromGallery = true")
        pickImageLauncher.launch("image/*")
    }

    val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.upload.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
                binding.generatedImage.setImageURI(it)
                myUri = it
            }
        }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openGallery() else Toast.makeText(
                requireContext(),
                "Permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }

    override fun onDestroy() {
        super.onDestroy(); _binding = null
    }
}
