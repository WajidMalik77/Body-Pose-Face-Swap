package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.editor

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.AdjustAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.ColorAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.EditorListAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.FilterAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.FontAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adjust.AdjustViewModel
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomAdjustBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomCropBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomRemoveBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.BottomTextBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentPhotoEditorBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.colorList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getEditorList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getFiltersList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getRatioList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getReStyleList
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getRotateList
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

class PhotoEditorFragment : Fragment() {

    private var _binding: FragmentPhotoEditorBinding? = null
    private val binding get() = _binding!!

    private val args: PhotoEditorFragmentArgs by navArgs()

    private val viewModel: AdjustViewModel by viewModels()
    private var bitmap1: Bitmap? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var textSticker: TextSticker
    var selectedColor = Color.WHITE
    var selectedFont: Typeface = Typeface.DEFAULT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoEditorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "PhotoEditorFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "PhotoEditorFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )
        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = getString(R.string.photo_editor)

        attachFirstImage()

        binding.upload.setOnClickListener {
            findNavController().navigate(
                PhotoEditorFragmentDirections.actionPhotoEditorFragmentToGalleryFragment(
                    featureType = FeatureType.IMAGE_EDITING,
                    imageSlot = ImageSlot.FIRST
                )
            )
        }

        binding.done.setOnClickListener {
            if (bitmap1 != null) navigateWithBitmap(
                bitmap1!!,
                R.id.action_photoEditorFragment_to_generatePictureFragment
            ) else Toast.makeText(
                requireContext(),
                getString(R.string.upload_image_first),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.bitmap.observe(viewLifecycleOwner) { bitmap ->
            bitmap ?: return@observe
            bitmap1 = bitmap
            binding.generatedImage.setImageBitmap(bitmap1)
        }

        binding.recycler.adapter =
            EditorListAdapter(requireContext().getEditorList()) { pos, name ->
                setAdjust(name, pos)
            }
    }


    private fun attachFirstImage() {
        val uri = args.selectedImageUri
        val resId = args.selectedResId

        when {
            uri.isNotEmpty() -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
                Glide.with(this).load(uri.toUri()).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    bitmap1 = withContext(Dispatchers.IO) {
                        runCatching { requireContext().uriToBitmap(uri.toUri()) }.getOrNull()
                    }
                    bitmap1?.let { binding.generatedImage.setImageBitmap(it) }
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                binding.done.visibility = View.VISIBLE
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
                                d.setBounds(0, 0, c.width, c.height); d.draw(c)
                            }
                            bmp
                        }.getOrNull()
                    }
                    bitmap1?.let { binding.generatedImage.setImageBitmap(it) }
                }
            }
        }
    }


    private fun setAdjust(title: String, pos: Int) {
        viewModel.setImage(bitmap1!!)
        when (pos) {
            0 -> showAdjustBottomSheet(title)
            1 -> showCropBottomSheet(title)
            2 -> showFiltersBottomSheet(title)
            3 -> showReStyleBottomSheet(title)
            4 -> showTextBottomSheet(title)
            5 -> showRemoveBgBottomSheet(title)
        }
    }

    private fun showAdjustBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val b = BottomAdjustBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply { setContentView(b.root) }
        b.title.text = title
        b.recyclerRotate.adapter = AdjustAdapter(requireContext().getRotateList()) {
            when (it) {
                0 -> viewModel.flipH(); 1 -> viewModel.rotateRight(); 2 -> viewModel.flipV()
            }
        }
        b.recycler.adapter = AdjustAdapter(requireContext().getRatioList()) { pos ->
            val currentBinding = _binding ?: return@AdjustAdapter
            val sourceBitmap = imageViewToBitmap(currentBinding.generatedImage) ?: return@AdjustAdapter
            startUCropFromBitmap(sourceBitmap, pos)
        }
        b.done.setOnClickListener { bottomSheetDialog?.dismiss() }
        b.cancel.setOnClickListener { bottomSheetDialog?.dismiss() }
        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showCropBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val b = BottomCropBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply { setContentView(b.root) }
        b.title.text = title
        b.recycler.adapter = AdjustAdapter(requireContext().getRatioList()) { pos ->
            val currentBinding = _binding ?: return@AdjustAdapter
            val sourceBitmap = imageViewToBitmap(currentBinding.generatedImage) ?: return@AdjustAdapter
            startUCropFromBitmap(sourceBitmap, pos)
        }
        b.done.setOnClickListener { bottomSheetDialog?.dismiss() }
        b.cancel.setOnClickListener { bottomSheetDialog?.dismiss() }
        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showFiltersBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val b = BottomCropBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply { setContentView(b.root) }
        b.title.text = title
        b.recycler.adapter =
            FilterAdapter(requireContext().getFiltersList()) { pos -> viewModel.applyFilter(pos) }
        b.done.setOnClickListener { bottomSheetDialog?.dismiss() }
        b.cancel.setOnClickListener { bottomSheetDialog?.dismiss() }
        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showReStyleBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val b = BottomCropBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply { setContentView(b.root) }
        b.title.text = title
        b.recycler.adapter = AdjustAdapter(requireContext().getReStyleList()) { _ ->
            var restyleStarted = false
            runWithRewardedGate(
                screen = "PhotoEditorFragmentScreen",
                trigger = "restyler",
                onAdShowing = {
                    if (!restyleStarted) {
                        restyleStarted = true
                        binding.progress.visibility = View.VISIBLE
                        GeminiImageService().changeFace(
                            apiKey = SharePref.getString(Constants.new_version_key, ""),
                            targetImage = imageViewToBitmap(binding.generatedImage)!!,
                            faceImage = BitmapFactory.decodeResource(
                                requireContext().resources,
                                R.drawable.ghibili
                            ),
                            prompt = "Re style the ghibli naturally",
                        ) { result ->
                            val currentBinding = _binding ?: return@changeFace
                            runWhenRewardedAdClosed {
                                result.onSuccess { bitmap ->
                                    currentBinding.progress.visibility = View.GONE
                                    bitmap1 = bitmap
                                    currentBinding.generatedImage.setImageBitmap(bitmap1)
                                }.onFailure {
                                    currentBinding.progress.visibility = View.GONE
                                    it.printStackTrace()
                                }
                            }
                        }
                    }
                }
            ) {
                if (!restyleStarted) {
                    restyleStarted = true
                    binding.progress.visibility = View.VISIBLE
                    GeminiImageService().changeFace(
                        apiKey = SharePref.getString(Constants.new_version_key, ""),
                        targetImage = imageViewToBitmap(binding.generatedImage)!!,
                        faceImage = BitmapFactory.decodeResource(
                            requireContext().resources,
                            R.drawable.ghibili
                        ),
                        prompt = "Re style the ghibli naturally",
                    ) { result ->
                        val currentBinding = _binding ?: return@changeFace
                        runWhenRewardedAdClosed {
                            result.onSuccess { bitmap ->
                                currentBinding.progress.visibility = View.GONE
                                bitmap1 = bitmap
                                currentBinding.generatedImage.setImageBitmap(bitmap1)
                            }.onFailure {
                                currentBinding.progress.visibility = View.GONE
                                it.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        b.done.visibility = View.GONE
        b.cancel.visibility = View.GONE
        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showTextBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val b = BottomTextBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply { setContentView(b.root) }
        b.title.text = title
        b.fontRecycler.adapter = FontAdapter(requireContext().loadFontsFromAssets()) { obj, _ ->
            selectedFont = requireContext().loadFontFromAssets(obj.assetFileName)
            b.editTextInput.typeface = selectedFont
        }
        b.recyclerColor.adapter = ColorAdapter(colorList) { color ->
            selectedColor = color
            b.editTextInput.setTextColor(selectedColor)
        }
        b.cancel.setOnClickListener { bottomSheetDialog?.dismiss() }
        b.done.setOnClickListener {
            val enteredText = b.editTextInput.text.toString().trim()
            if (enteredText.isNotEmpty()) {
                val ts = TextSticker(requireContext()).apply {
                    setText(enteredText); setTextColor(selectedColor); setTypeface(selectedFont)
                    setTextAlign(Layout.Alignment.ALIGN_CENTER); resizeText()
                }
                binding.stickerView.addSticker(ts)
                bitmap1 = binding.stickerView.createBitmap()
                bottomSheetDialog?.dismiss()
            } else Toast.makeText(context, "Please enter some text", Toast.LENGTH_SHORT).show()
        }
        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showRemoveBgBottomSheet(title: String) {
        if (!isAdded || isDetached || bottomSheetDialog?.isShowing == true) return
        val b = BottomRemoveBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply { setContentView(b.root) }
        b.title.text = title
        b.generate.setOnClickListener {
            var bgRemoveStarted = false
            runWithRewardedGate(
                screen = "PhotoEditorFragmentScreen",
                trigger = "bg_remove",
                onAdShowing = {
                    if (!bgRemoveStarted) {
                        bgRemoveStarted = true
                        val bitmap = binding.generatedImage.drawable.toBitmap()
                        binding.progress.visibility = View.VISIBLE
                        GeminiImageService().removeBackgroundWithGemini(
                            SharePref.getString(Constants.new_version_key, ""), bitmap
                        ) { result ->
                            val currentBinding = _binding ?: return@removeBackgroundWithGemini
                            runWhenRewardedAdClosed {
                                result.onSuccess { bmp ->
                                    currentBinding.progress.visibility = View.GONE
                                    bitmap1 = bmp
                                    currentBinding.generatedImage.setImageBitmap(bitmap1)
                                }
                                result.onFailure { error ->
                                    currentBinding.progress.visibility = View.GONE
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
            ) {
                if (!bgRemoveStarted) {
                    bgRemoveStarted = true
                    val bitmap = binding.generatedImage.drawable.toBitmap()
                    binding.progress.visibility = View.VISIBLE
                    GeminiImageService().removeBackgroundWithGemini(
                        SharePref.getString(Constants.new_version_key, ""), bitmap
                    ) { result ->
                        val currentBinding = _binding ?: return@removeBackgroundWithGemini
                        runWhenRewardedAdClosed {
                            result.onSuccess { bmp ->
                                currentBinding.progress.visibility = View.GONE
                                bitmap1 = bmp
                                currentBinding.generatedImage.setImageBitmap(bitmap1)
                            }
                            result.onFailure { error ->
                                currentBinding.progress.visibility = View.GONE
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
        }
        bottomSheetDialog?.show()
        (bottomSheetDialog?.window?.decorView?.parent as? ViewGroup)?.setPadding(0, 0, 0, 0)
        bottomSheetDialog?.window?.setGravity(Gravity.BOTTOM)
    }

    fun addText(text: String) {
        textSticker = TextSticker(requireContext()).apply {
            this.text = text; setTextColor(selectedColor)
            setTextAlign(Layout.Alignment.ALIGN_CENTER); resizeText(); setTypeface(selectedFont)
        }
        binding.stickerView.addSticker(textSticker)
        bitmap1 = binding.stickerView.createBitmap()
        binding.generatedImage.setImageBitmap(bitmap1)
    }

    // ── UCrop — unchanged ──────────────────────────────────────

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                UCrop.getOutput(result.data!!)?.let {
                    bitmap1 = requireContext().contentResolver.openInputStream(it)
                        ?.use { input -> BitmapFactory.decodeStream(input) }
                    binding.generatedImage.setImageBitmap(bitmap1)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                Toast.makeText(
                    requireContext(),
                    UCrop.getError(result.data!!)?.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    fun startUCropFromBitmap(bitmap: Bitmap, pos: Int) {
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true); setHideBottomControls(false); setShowCropGrid(true)
            setAspectRatioOptions(
                pos,
                AspectRatio("Original", 0f, 0f),
                AspectRatio("Square", 1f, 1f),
                AspectRatio("2:3", 2f, 3f),
                AspectRatio("3:4", 3f, 4f),
                AspectRatio("5:7", 5f, 7f),
                AspectRatio("9:16", 9f, 16f)
            )
        }
        cropLauncher.launch(
            UCrop.of(
                bitmap.toCacheUri(requireContext()),
                Uri.fromFile(
                    File(
                        requireContext().cacheDir,
                        "crop_${System.currentTimeMillis()}.jpg"
                    )
                )
            ).withOptions(options).getIntent(requireContext())
        )
    }

    override fun onDestroyView() {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        super.onDestroyView()
        _binding = null
    }
}
