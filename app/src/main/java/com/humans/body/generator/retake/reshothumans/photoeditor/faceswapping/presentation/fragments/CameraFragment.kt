package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.MyApplication
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.AdapterPose
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.AdjustAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.ColorAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.EditorListAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.FontAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.VariotionImageAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.VariotionTextAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleAdult
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleChild
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleTeen
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleAdult
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleChild
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleTeen
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomCropBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomPoseBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomTextBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomVariationBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentCameraBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.colorList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getCameraList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getRatioList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getVariationNumber
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.loadFontFromAssets
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.loadFontsFromAssets
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.navigateWithBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.toCacheUri
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import com.xiaopo.flying.sticker.TextSticker
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CameraFragment : Fragment() {
    private var bitmap1: Bitmap? = null

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCameraBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "CameraFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "CameraFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        binding.toolbar.imageView.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.done.setOnClickListener {
            if (bitmap1 != null) {
                runWithRewardedGate("CameraFragmentScreen", "done") {
                    navigateWithBitmap(
                        bitmap1!!,
                        R.id.action_cameraFragment_to_generatePictureFragment
                    )
                }
            } else
                Toast.makeText(
                    requireContext(),
                    getString(R.string.upload_image_first), Toast.LENGTH_SHORT
                ).show()
        }
        checkCameraPermission()
        binding.recycler.adapter =
            EditorListAdapter(requireContext().getCameraList()) { pos, name ->
                when (pos) {
                    0 -> {
                        showPoseBottomSheet(name)
                    }

                    1 -> { /*edit*/
                        showTextBottomSheet(name)
                    }

                    2 -> {/*variation*/
                        showVariationBottomSheet(name)
                    }

                    3 -> {
                        binding.recycler.visibility = View.GONE
                        binding.progress.visibility = View.VISIBLE
                        val bitmap =
                            binding.generatedImage.drawable.toBitmap() // convert ImageView drawable to Bitmap

                        GeminiImageService().removeBackgroundWithGemini(
                            SharePref.getString(Constants.new_version_key, ""),
                            bitmap,
                            "Resize this image to 1024x1024 Keep the subject unchanged Preserve colors, lighting, and details No artistic changes."
                        ) { result ->
                            requireActivity().runOnUiThread {
                                result.onSuccess { bitmap ->
                                    if (isAdded) {
                                        binding.recycler.visibility = View.VISIBLE
                                        binding.progress.visibility = View.GONE
                                        bitmap1 = bitmap

                                        binding.generatedImage.setImageBitmap(bitmap)  // image without background
                                    }
                                }
                                result.onFailure { error ->
                                    if (isAdded) {
                                        binding.recycler.visibility = View.VISIBLE
                                        binding.progress.visibility = View.GONE
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

                    4 -> {
                        binding.recycler.visibility = View.GONE
                        binding.progress.visibility = View.VISIBLE
                        /*ai resize*/
                        val bitmap =
                            binding.generatedImage.drawable.toBitmap() // convert ImageView drawable to Bitmap

                        GeminiImageService().removeBackgroundWithGemini(
                            SharePref.getString(Constants.new_version_key, ""),
                            bitmap
                        ) { result ->
                            requireActivity().runOnUiThread {
                                result.onSuccess { bitmap ->
                                    if (isAdded) {
                                        binding.recycler.visibility = View.VISIBLE
                                        binding.progress.visibility = View.GONE
                                        bitmap1 = bitmap

                                        binding.generatedImage.setImageBitmap(bitmap)  // image without background
                                    }
                                }
                                result.onFailure { error ->
                                    if (isAdded) {
                                        binding.recycler.visibility = View.VISIBLE
                                        binding.progress.visibility = View.GONE
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

                    5 -> {
                        binding.recycler.visibility = View.GONE
                        binding.progress.visibility = View.VISIBLE
                        GeminiImageService().changeFace(
                            apiKey = SharePref.getString(Constants.new_version_key, ""),
                            targetImage = imageViewToBitmap(binding.generatedImage)!!,
                            faceImage = BitmapFactory.decodeResource(
                                requireContext().resources,
                                R.drawable.ghibili
                            ),
                            prompt = "Re style the ghibli naturally",
                        )
                        { result ->
                            result.onSuccess { bitmap ->
                                if (isAdded) {
                                    binding.recycler.visibility = View.VISIBLE
                                    binding.progress.visibility = View.GONE
                                    bitmap1 = bitmap

                                    Log.d("TAG", "handleImageUri: onSuccess")

//                    binding.upload.visibility = View.VISIBLE
//                    binding.upload1.visibility = View.VISIBLE
//                binding.genderContainer.visibility = View.VISIBLE
//                binding.scrollView4.visibility = View.VISIBLE
//                binding.recycler.visibility = View.VISIBLE
//                    binding.generate.visibility = View.VISIBLE
//                    binding.progress.visibility = View.GONE
//                btnGenerate.text="Done"
                                    binding.generatedImage.setImageBitmap(bitmap)
                                }
                                /*   val outputStream = ByteArrayOutputStream()
                                   bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                   val byteArray = outputStream.toByteArray()

                                   val bundle = Bundle().apply {
                                       putByteArray("BITMAP_BYTES", byteArray)
                                   }
                                   findNavController().navigate(
                                       R.id.action_faceSwapFragment_to_generatePictureFragment,
                                       bundle
                                   )*/
//                binding.generatedImage.setImageBitmap(bitmap)
                            }.onFailure {
                                if (isAdded) {
                                    binding.recycler.visibility = View.VISIBLE
                                    binding.progress.visibility = View.GONE
                                    Log.d("TAG", "handleImageUri: onFailure")
//                btnGenerate.text="error ${it.message}"
//                    binding.upload.visibility = View.VISIBLE
//                binding.genderContainer.visibility = View.VISIBLE
//                binding.scrollView4.visibility = View.VISIBLE
//                binding.recycler.visibility = View.VISIBLE
//                    binding.generate.visibility = View.VISIBLE
//                    binding.progress.visibility = View.GONE
                                    it.printStackTrace()
                                }
                            }
                        }
                    }

                    6 -> {/*crop*/
                        showCropBottomSheet(requireContext().getString(R.string.crop))
                    }

                    7 -> {
                        binding.recycler.visibility = View.GONE
                        binding.progress.visibility = View.VISIBLE
                        GeminiImageService().upscaleImageWithGemini(
                            SharePref.getString(Constants.new_version_key, ""),
                            binding.generatedImage.drawable.toBitmap()
                        ) { result ->
                            requireActivity().runOnUiThread {
                                result.onSuccess { upscaled ->
                                    if (isAdded) {
                                        binding.recycler.visibility = View.VISIBLE
                                        binding.progress.visibility = View.GONE
                                        bitmap1 = upscaled

                                        binding.generatedImage.setImageBitmap(upscaled)
                                    }
                                }
                                result.onFailure {
                                    if (isAdded) {
                                        binding.recycler.visibility = View.VISIBLE
                                        binding.progress.visibility = View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Upscale failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }

                    }
                }

            }
    }

    var selectedColor = Color.WHITE
    var selectedFont: Typeface = Typeface.DEFAULT
    private fun showTextBottomSheet(title: String) {

        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        // Inflate the bottom sheet layout
        val bottomSheetBinding = BottomTextBinding.inflate(layoutInflater)

        // Create the BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(bottomSheetBinding.root)
        bottomSheetBinding.title.text = title
//        bottomSheetBinding.colorPicker.setColorListener { color, _ ->
//            selectedColor = color
//            if (::textSticker.isInitialized) {
//                textSticker.setTextColor(color)
//                binding.stickerView.invalidate()
//            }
//        }
        bottomSheetBinding.fontRecycler.adapter =
            FontAdapter(requireContext().loadFontsFromAssets()) { obj, name ->
//                selectedFont =
//                    Typeface.createFromAsset(requireContext().assets, "${obj.assetFileName}")
//            if (::textSticker.isInitialized) {
//                textSticker.setTypeface(selectedFont)
//                binding.stickerView.invalidate()
//            }
                selectedFont = requireContext().loadFontFromAssets(obj.assetFileName)
                bottomSheetBinding.editTextInput.typeface = selectedFont

            }
        bottomSheetBinding.recyclerColor.adapter =
            ColorAdapter(colorList) { color ->
                selectedColor = color
//                textSticker.setTextColor(color)
                bottomSheetBinding.editTextInput.setTextColor(selectedColor)

            }
        bottomSheetBinding.done.setOnClickListener {
            val enteredText = bottomSheetBinding.editTextInput.text.toString().trim()
            if (enteredText.isNotEmpty()) {
                val textSticker = TextSticker(requireContext()).apply {
                    setText(enteredText)
                    setTextColor(selectedColor)
                    setTypeface(selectedFont)
                    setTextAlign(Layout.Alignment.ALIGN_CENTER)
                    resizeText()
                }

                binding.stickerView.addSticker(textSticker)
                val finalBitmap = binding.stickerView.createBitmap()
                bitmap1 = finalBitmap
//                textInputListener?.onTextEntered(enteredText, selectedColor)
            } else {
                Toast.makeText(context, "Please enter some text", Toast.LENGTH_SHORT).show()
            }
        }

//        bottomSheetBinding.textSizeSeek.max = 100
//
//        bottomSheetBinding.textSizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (::textSticker.isInitialized) {
////                    textSticker.textSize = progress + 20f
//                    textSticker.resizeText()
//                    binding.stickerView.invalidate()
//                }
//            }
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })


//        EditorListAdapter(requireContext().getRatioList()) { pos, name ->
//
//        }


        bottomSheetDialog?.show()

        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)

//        bottomSheetDialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
    }


    private var bottomSheetDialog: BottomSheetDialog? = null
    private fun showCropBottomSheet(title: String) {

        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        // Inflate the bottom sheet layout
        val bottomSheetBinding = BottomCropBinding.inflate(layoutInflater)

        // Create the BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(bottomSheetBinding.root)
        bottomSheetBinding.title.text = title
        bottomSheetBinding.recycler.adapter = AdjustAdapter(
            requireContext().getRatioList()
        ) { pos ->
            startUCropFromBitmap(imageViewToBitmap(binding.generatedImage)!!, pos)
        }
        bottomSheetBinding.done.setOnClickListener { bottomSheetDialog?.dismiss() }
        bottomSheetBinding.cancel.setOnClickListener { bottomSheetDialog?.dismiss() }

//        EditorListAdapter(requireContext().getRatioList()) { pos, name ->
//
//        }


        bottomSheetDialog?.show()

        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)

//        bottomSheetDialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
    }

    private var count: Int = 0

    private fun showVariationBottomSheet(title: String) {

        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        // Inflate the bottom sheet layout
        val bottomSheetBinding = BottomVariationBinding.inflate(layoutInflater)

        // Create the BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(bottomSheetBinding.root)
        bottomSheetBinding.title.text = title
        bottomSheetBinding.recyclerText.adapter =
            VariotionTextAdapter(requireContext().getVariationNumber()) { pos ->

                count = pos
                bottomSheetBinding.recyclerText.visibility = View.GONE
                bottomSheetBinding.progressBar.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    val bitmap =
                        binding.generatedImage.drawable.toBitmap() // convert ImageView drawable to Bitmap

                    GeminiImageService().generateImageVariations(
                        SharePref.getString(Constants.new_version_key, ""),
                        bitmap, count
                    )
                    { result ->
                        if (!isAdded) return@generateImageVariations
                        requireActivity().runOnUiThread {
                            result.onSuccess { bitmap ->
                                if (isAdded) {
                                    bottomSheetBinding.progressBar.visibility = View.GONE
                                    bottomSheetBinding.recyclerImage.visibility = View.VISIBLE

                                    Log.d("TAG", "recyclerImage: ${bitmap.size}")
                                    bottomSheetBinding.recyclerImage.adapter =
                                        VariotionImageAdapter(bitmap) { bitmap2 ->
                                            bitmap1 = bitmap2

                                            binding.generatedImage.setImageBitmap(bitmap1)  // image without background

                                        }

//                                    bitmap1 = bitmap
//
                                }
                            }
                            result.onFailure { error ->
                                if (isAdded) {
                                    bottomSheetBinding.progressBar.visibility = View.GONE
                                    bottomSheetBinding.recyclerImage.visibility = View.VISIBLE

                                    Toast.makeText(
                                        requireContext(),
                                        "Failed: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }, 1000)

            }







        bottomSheetDialog?.show()

        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)

    }

    private fun showPoseBottomSheet(title: String) {

        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        // Inflate the bottom sheet layout
        val bottomSheetBinding = BottomPoseBinding.inflate(layoutInflater)

        // Create the BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(bottomSheetBinding.root)
        bottomSheetBinding.title.text = title
        bottomSheetBinding.recycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            setHasFixedSize(true)
        }

        /*
                val adapter = AdapterPose(getMaleTeen()) { faceImage ->
                    bottomSheetDialog?.dismiss()

                    binding.recycler.visibility = View.GONE
                    binding.progress.visibility = View.VISIBLE
                    GeminiImageService().changeFace(
                        apiKey = "AIzaSyCAVVsPMMyCUhU8xIPFoJjIP-RglKMBpMY",
                        targetImage = imageViewToBitmap(binding.generatedImage)!!,
                        faceImage = imageViewToBitmap(faceImage)!!,
                        prompt = "Replace the pose naturally",
                    )
                    { result ->
                        result.onSuccess { bitmap ->
                            if (isAdded) {
                                bitmap1 = bitmap
                                binding.generatedImage.setImageBitmap(bitmap)
                            }
                        }.onFailure {
                            if (isAdded) {
        //                btnGenerate.text="error ${it.message}"
                                binding.recycler.visibility = View.VISIBLE
                                binding.progress.visibility = View.GONE
                                it.printStackTrace()
                            }
                        }
                    }

                }
        */
        val adapter = AdapterPose(getMaleTeen()) { faceImage ->

            bottomSheetDialog?.dismiss()

            binding.recycler.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE

            viewLifecycleOwner.lifecycleScope.launch {

                // 1️⃣ Prepare bitmaps off main thread
                val targetBitmap = withContext(Dispatchers.Default) {
                    imageViewToBitmap(binding.generatedImage)
                }

                val faceBitmap = withContext(Dispatchers.Default) {
                    imageViewToBitmap(faceImage)
                }

                if (targetBitmap == null || faceBitmap == null) {
                    binding.recycler.visibility = View.VISIBLE
                    binding.progress.visibility = View.GONE
                    return@launch
                }

                // 2️⃣ Call Gemini (already async)
                GeminiImageService().changeFace(
                    apiKey = SharePref.getString(Constants.new_version_key, ""),
                    targetImage = targetBitmap,
                    faceImage = faceBitmap,
                    prompt = "Replace the pose naturally while preserving identity."
                ) { result ->

                    if (!isAdded) return@changeFace

                    requireActivity().runOnUiThread {

                        binding.progress.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE

                        result.onSuccess { bitmap ->
                            bitmap1 = bitmap
                            binding.generatedImage.setImageBitmap(bitmap)
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }
                }
            }
        }

        bottomSheetBinding.recycler.adapter = adapter
        val genderOptions = listOf("Female", "Male")
        addGenderOptions(genderOptions, bottomSheetBinding.genderContainer, adapter)
        val ages =
            listOf("Adult", "Teenage",/* "Young adult",*/ "Child" /*"Middle-aged",*/ /*"Infant"*/)
        ages.forEach { bottomSheetBinding.ageContainer.addView(createAgeChip(it, adapter)) }





        bottomSheetDialog?.show()

        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)

    }

    private var selectedChip: TextView? = null

    private fun createAgeChip(text: String, optionAdapter: AdapterPose): TextView {
        val context = requireContext()
        val chip = TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(18, 15, 18, 15)

            background = ContextCompat.getDrawable(context, R.drawable.custom_text_view)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 16, 0)
            this.layoutParams = layoutParams

            setOnClickListener {
                selectedChip?.background =
                    ContextCompat.getDrawable(context, R.drawable.custom_text_view)


                background =
                    ContextCompat.getDrawable(context, R.drawable.custom_selected_text_view)

                selectedChip = this
                ageText = text
                Log.d("TAG", "addGenderOptions: $text")
//                if (::optionAdapter.isInitialized) {
                updateRecyclerData(optionAdapter, genderText, ageText)
//                }
//                addKeywordChip("Age", text)
            }
        }

        return chip
    }

    private var ageText = ""
    private var genderText = "Female"
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

                for (i in 0 until container.childCount) {
                    val child = container.getChildAt(i)
                    val icon = child.findViewById<ImageView>(R.id.checkIcon)
                    icon?.setImageResource(R.drawable.ic_uncheck)
                }

                val icon = selectedView.findViewById<ImageView>(R.id.checkIcon)
                icon?.setImageResource(R.drawable.ic_check)
            }

            container.addView(optionView)
        }
    }

    private fun updateRecyclerData(
        adapter: AdapterPose,
        gender: String = "Female",
        age: String?
    ) {
        Log.d("TAG", "updateRecyclerData: $gender  , $age")
        val data = when {
            gender == "Male" && (age == "Teenage") -> getMaleTeen()
            gender == "Male" && age == "Adult" -> getMaleAdult()
            gender == "Male" && age == "Child" -> getMaleChild()
            gender == "Female" && (age == "Teenage") -> getFemaleTeen()
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
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_gender_option, null)

        val label = view.findViewById<TextView>(R.id.label)
        val icon = view.findViewById<ImageView>(R.id.checkIcon)

        label.text = gender
        icon.setImageResource(if (isSelected) R.drawable.ic_check else R.drawable.ic_uncheck)

        view.tag = gender

        view.setOnClickListener {
            val selectedGender = it.tag as? String
            if (selectedGender != null) {
                onSelected(it)
            }
        }

        return view
    }

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val uri = UCrop.getOutput(result.data!!)
                uri?.let {
                    val bitmap = requireContext().contentResolver.openInputStream(it)
                        ?.use { input -> BitmapFactory.decodeStream(input) }

                    binding.generatedImage.setImageBitmap(bitmap)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val error = UCrop.getError(result.data!!)
                Toast.makeText(requireContext(), error?.message, Toast.LENGTH_LONG).show()
            }
        }

    fun startUCropFromBitmap(bitmap: Bitmap, pos: Int) {

        val sourceUri = bitmap.toCacheUri(requireContext())
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "crop_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {

            setFreeStyleCropEnabled(true)
            setHideBottomControls(false)
            setShowCropGrid(true)

            setAspectRatioOptions(
                pos,
                AspectRatio("Original", 0f, 0f),
                AspectRatio("Square", 1f, 1f),
                AspectRatio("2:3", 2f, 3f),
                AspectRatio("3:4", 3f, 4f),
                AspectRatio("5:7", 5f, 7f),
                AspectRatio("9:16", 9f, 16f),
//                AspectRatio("16:9", 16f, 9f)
            )
        }

        val intent = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .getIntent(requireContext())

        cropLauncher.launch(intent)
    }

    private lateinit var cameraImageUri: Uri

    @RequiresApi(Build.VERSION_CODES.P)
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val bitmap = requireContext().uriToBitmap(cameraImageUri)
                binding.generatedImage.setImageBitmap(bitmap)
                binding.recycler.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun openCamera() {
        val imageFile = File(
            requireContext().cacheDir,
            "camera_${System.currentTimeMillis()}.jpg"
        )

        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )

        cameraLauncher.launch(cameraImageUri)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                .show()
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkCameraPermission() {
        MyApplication.isResume = true
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
