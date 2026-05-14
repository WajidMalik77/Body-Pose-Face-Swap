package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.faceStyle

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.BillingManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.RetrofitClient
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentFaceStyleBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.repositories.ImageRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.ResultHolder
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.uriToBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.viewmodels.ImageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FaceStyleFragment : Fragment(), BillingManager.PurchaseListener {

    private var _binding: FragmentFaceStyleBinding? = null
    private val binding get() = _binding!!

    private val args: FaceStyleFragmentArgs by navArgs()

    private val selectedChipsMap = mutableMapOf<String, String>()
    private var selectionDialog: AlertDialog? = null
    private var selectedChip: TextView? = null
    private var selectedPoseChip: TextView? = null
    private var selectedTargetImageView: ImageView? = null
    private var selectedTargetFrameImageView: ImageView? = null
    private var isGenerating = false
    private val repository by lazy { ImageRepository(RetrofitClient.api) }
    private val viewModel: ImageViewModel by lazy { ImageViewModel(repository) }

    private var firstBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceStyleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "FaceStyleFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "FaceStyleFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        binding.generate.visibility = View.VISIBLE
        binding.generate.isEnabled = false
        binding.generate.alpha = 0.5f

        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }

        attachFirstImage()

        binding.upload.setOnClickListener {
            findNavController().navigate(
                FaceStyleFragmentDirections.actionFaceStyleFragmentToGalleryFragment(
                    featureType = FeatureType.FACE_STYLE,
                    imageSlot = ImageSlot.FIRST
                )
            )
        }

        binding.generate.setOnClickListener {
            if (firstBitmap == null) {
                Toast.makeText(requireContext(), "Upload Image first", Toast.LENGTH_SHORT)
                    .show(); return@setOnClickListener
            }
            var generationStarted = false
            runWithRewardedGate(
                screen = "FaceStyleFragmentScreen",
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

        selectedTargetImageView = binding.imageView5
        selectedTargetFrameImageView = binding.imageView6

        addGenderOptions(listOf("Female", "Male"))
        listOf(
            "Emotion",
            "Skin Tone",
            "Hair Color",
            "Hair Length",
            "Glasses",
            "Makeup"
        ).forEach { binding.optionsContainer.addView(createChip(it)) }
        listOf(
            "Adult",
            "Young adult",
            "Child",
            "Middle-aged",
            "Infant"
        ).forEach { binding.ageContainer.addView(createAgeChip(it)) }
        listOf("Center", "Right", "Left", "Up", "Down").forEach {
            binding.poseContainer.addView(
                createPoseChip(it)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                    enableGenerate()
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    firstBitmap =
                        withContext(Dispatchers.IO) { runCatching { drawableToBitmap(resId) }.getOrNull() }
                    enableGenerate()
                }
            }
        }
    }

    private fun enableGenerate() {
        if (!isAdded) return
        if (firstBitmap != null) {
            binding.generate.isEnabled = true; binding.generate.alpha = 1f
        }
    }

    // ── ONLY success block changed ─────────────────────────────

    private fun handleImageUri() {
        val target = firstBitmap ?: return

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.generate.visibility = View.GONE

        GeminiImageService().changeFace(
            apiKey = SharePref.getString(Constants.new_version_key, ""),
            targetImage = target,
            faceImage = imageViewToBitmap(binding.imageView5)!!,
            prompt = "Replace the face pose naturally and also " + getCombinedPrompt(),
        ) { result ->
            if (!isAdded) return@changeFace
            runWhenRewardedAdClosed {
                binding.loadingOverlay.visibility = View.GONE
                binding.generate.visibility = View.VISIBLE
                result
                    .onSuccess { bitmap ->
                        ResultHolder.beforeBitmap = target
                        ResultHolder.afterBitmap = bitmap
                        findNavController().navigate(R.id.action_faceStyleFragment_to_beforeAfterFragment)
                    }
                    .onFailure { it.printStackTrace() }
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

    private fun getCombinedPrompt(): String {
        val instructions = mutableListOf<String>()
        for (i in 0 until binding.keywordContainer.childCount) {
            val view = binding.keywordContainer.getChildAt(i)
            if (view is TextView) {
                val value = view.text.toString().trim()
                when (value.lowercase()) {
                    "male", "female" -> instructions.add("Ensure the subject is clearly $value without changing identity.")
                    "child" -> instructions.add("Make the person look like a child while preserving facial identity.")
                    "infant" -> instructions.add("Make the person look like a Infant while preserving facial identity.")
                    "young adult" -> instructions.add("Make the person appear young naturally.")
                    "middle-aged" -> instructions.add("Make the person look Middle aged with realistic aging details.")
                    "center", "right", "left", "up", "down" -> instructions.add("Adjust the face and body pose to appear $value naturally.")
                    "happy", "sad", "angry", "surprised", "neutral", "contemptuous", "disgusted", "Frightened" -> instructions.add(
                        "Change facial expression to look $value naturally."
                    )

                    "white", "gray", "black", "brown", "blonde", "ginger" -> instructions.add("Change hair color to natural $value.")
                    "short", "medium", "long" -> instructions.add("Adjust hair length to $value.")
                    "reading glasses", "sunglasses" -> instructions.add("Add realistic $value that fit the face naturally.")
                    "eyes", "lips" -> instructions.add("Apply subtle $value makeup only, realistic and natural.")
                    else -> instructions.add("Apply $value naturally.")
                }
            }
        }
        return instructions.joinToString("\n")
    }

    private fun createChip(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text; setTextColor(Color.WHITE); textSize = 14f; gravity =
            Gravity.CENTER; setPadding(18, 15, 18, 15)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_text_view)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 16, 16, 0) }
            val chipRef = this
            setOnClickListener {
                when (text) {
                    "Emotion" -> showCustomRadioDialog(
                        "Emotion",
                        listOf(
                            "Natural",
                            "Happy",
                            "Surprised",
                            "Angry",
                            "Contemptuous",
                            "Disgusted",
                            "Frightened",
                            "Sad"
                        )
                    ) { selected ->
                        chipRef.background = ContextCompat.getDrawable(
                            requireContext(),
                            if (selected != null) R.drawable.custom_selected_text_view else R.drawable.custom_text_view
                        )
                    }

                    "Skin Tone" -> showCustomRadioDialog(
                        "Skin Tone",
                        listOf(
                            "Porcelain",
                            "Rosy",
                            "Peach",
                            "White",
                            "Neutral",
                            "Golden",
                            "Olive",
                            "Tan",
                            "Caramel",
                            "Bronze",
                            "Mahogany",
                            "Dark Mahogany",
                            "Cocoa",
                            "Dark",
                            "Onyx Black",
                            "Black Purple",
                            "Ethnicity",
                            "Clear",
                            "Albanian"
                        )
                    ) { selected ->
                        chipRef.background = ContextCompat.getDrawable(
                            requireContext(),
                            if (selected != null) R.drawable.custom_selected_text_view else R.drawable.custom_text_view
                        )
                    }

                    "Hair Color" -> showCustomRadioDialog(
                        "Hair Color",
                        listOf("Black", "Brown", "Blonde", "Ginger", "Gray", "White")
                    ) { selected ->
                        chipRef.background = ContextCompat.getDrawable(
                            requireContext(),
                            if (selected != null) R.drawable.custom_selected_text_view else R.drawable.custom_text_view
                        )
                    }

                    "Hair Length" -> showCustomRadioDialog(
                        "Hair Length",
                        listOf("Short", "Medium", "Large")
                    ) { selected ->
                        chipRef.background = ContextCompat.getDrawable(
                            requireContext(),
                            if (selected != null) R.drawable.custom_selected_text_view else R.drawable.custom_text_view
                        )
                    }

                    "Glasses" -> showCustomRadioDialog(
                        "Glasses",
                        listOf("Reading Glasses", "Sunglasses")
                    ) { selected ->
                        chipRef.background = ContextCompat.getDrawable(
                            requireContext(),
                            if (selected != null) R.drawable.custom_selected_text_view else R.drawable.custom_text_view
                        )
                    }

                    "Makeup" -> showCustomRadioDialog(
                        "Makeup",
                        listOf("Eyes", "Lips")
                    ) { selected ->
                        chipRef.background = ContextCompat.getDrawable(
                            requireContext(),
                            if (selected != null) R.drawable.custom_selected_text_view else R.drawable.custom_text_view
                        )
                    }
                }
            }
        }
    }

    private fun createAgeChip(text: String): TextView {
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
                selectedChip = this; addKeywordChip("Age", text)
            }
        }
    }

    private fun createPoseChip(text: String): TextView {
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
                selectedPoseChip?.background =
                    ContextCompat.getDrawable(context, R.drawable.custom_text_view)
                background =
                    ContextCompat.getDrawable(context, R.drawable.custom_selected_text_view)
                selectedPoseChip = this; addKeywordChip("Pose", text)
                selectedTargetImageView?.setImageResource(
                    when (text) {
                        "Center" -> R.drawable.ic_center_pose; "Right" -> R.drawable.ic_right_pose; "Left" -> R.drawable.ic_left_pose; "Up" -> R.drawable.ic_up_pose; "Down" -> R.drawable.ic_down_pose; else -> R.drawable.ic_center_pose
                    }
                )
                selectedTargetFrameImageView?.setImageResource(
                    when (text) {
                        "Center" -> R.drawable.ic_center_pose_frame; "Right" -> R.drawable.ic_right_pose_frame; "Left" -> R.drawable.ic_left_pose_frame; "Up" -> R.drawable.ic_up_pose_frame; "Down" -> R.drawable.ic_down_pose_frame; else -> R.drawable.ic_center_pose_frame
                    }
                )
            }
        }
    }

    private fun showCustomRadioDialog(
        category: String,
        options: List<String>,
        onSelected: (String?) -> Unit
    ) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choice, null)
        val titleView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val clearPromptView = dialogView.findViewById<TextView>(R.id.clear_prompt)
        val container = dialogView.findViewById<LinearLayout>(R.id.optionContainer)
        titleView.text = category; titleView.setTextColor(Color.WHITE)
        var selectedLayout: LinearLayout? = null
        options.forEach { option ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL; gravity =
                Gravity.CENTER_VERTICAL; setPadding(8, 8, 8, 8); layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val textView = TextView(requireContext()).apply {
                text = option; setTextColor(Color.WHITE); textSize = 16f; setPadding(
                5,
                5,
                0,
                5
            ); layoutParams =
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val imageView = ImageView(requireContext()).apply {
                id = R.id.radioIcon; setImageResource(R.drawable.ic_radio_ellipse); layoutParams =
                LinearLayout.LayoutParams(48, 48)
            }
            itemLayout.addView(textView); itemLayout.addView(imageView)
            itemLayout.setOnClickListener {
                (selectedLayout?.getChildAt(1) as? ImageView)?.setImageResource(
                    R.drawable.ic_radio_ellipse
                ); imageView.setImageResource(R.drawable.ic_check); selectedLayout =
                itemLayout; onSelected(option); addKeywordChip(
                category,
                option
            ); selectionDialog?.dismiss()
            }
            container.addView(itemLayout)
        }
        selectionDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        clearPromptView.setOnClickListener {
            (selectedLayout?.findViewById<ImageView>(R.id.radioIcon))?.setImageResource(
                R.drawable.ic_radio_ellipse
            ); selectedLayout =
            null; onSelected(null); binding.keywordContainer.children.firstOrNull { it is TextView && it.tag == category }
            ?.let { binding.keywordContainer.removeView(it) }; selectedChipsMap.remove(category); selectionDialog?.dismiss()
        }
        selectionDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        selectionDialog?.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addKeywordChip(category: String, text: String) {
        val existingChip =
            binding.keywordContainer.children.firstOrNull { it is TextView && it.tag == category } as? TextView
        if (existingChip != null) {
            existingChip.text = text; selectedChipsMap[category] = text; return
        }
        val chip = TextView(requireContext()).apply {
            this.text = text; this.tag = category; setPadding(
            12,
            12,
            12,
            12
        ); setTextColor(Color.WHITE); textSize = 14f
            background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_prompt_view
            ); isClickable = true; isFocusable = true
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(context, R.drawable.ic_cross),
                null
            ); compoundDrawablePadding = 12
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(10, 10, 10, 10) }
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val de =
                        compoundDrawables[2]; if (de != null && event.x >= width - paddingEnd - de.bounds.width()) {
                        binding.keywordContainer.removeView(this); selectedChipsMap.remove(category); performClick(); return@setOnTouchListener true
                    }
                }
                false
            }
        }
        binding.keywordContainer.addView(chip); selectedChipsMap[category] = text
        (binding.keywordContainer.parent as? HorizontalScrollView)?.post {
            (binding.keywordContainer.parent as HorizontalScrollView).smoothScrollTo(
                chip.right,
                0
            )
        }
    }

    private fun addGenderOptions(options: List<String>) {
        val container = binding.genderContainer
        options.forEach { option ->
            val isSelected = option == "Female"
            val optionView = createGenderOption(option, isSelected) { selectedView ->
                val selectedText = selectedView.tag as String
                if (binding.keywordContainer.children.firstOrNull { it is TextView && it.text == selectedText } != null) return@createGenderOption
                addKeywordChip("Gender", selectedText)
                for (i in 0 until container.childCount) {
                    container.getChildAt(i).findViewById<ImageView>(R.id.checkIcon)
                        ?.setImageResource(R.drawable.ic_uncheck)
                }
                selectedView.findViewById<ImageView>(R.id.checkIcon)
                    ?.setImageResource(R.drawable.ic_check)
            }
            container.addView(optionView)
            if (isSelected) addKeywordChip("Gender", option)
        }
    }

    private fun createGenderOption(
        gender: String,
        isSelected: Boolean = false,
        onSelected: (View) -> Unit
    ): View {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_gender_option, null)
        val label = view.findViewById<TextView>(R.id.label)
        val icon = view.findViewById<ImageView>(R.id.checkIcon)
        label.text =
            gender; icon.setImageResource(if (isSelected) R.drawable.ic_check else R.drawable.ic_uncheck)
        view.tag =
            gender; view.setOnClickListener { (it.tag as? String)?.let { _ -> onSelected(it) } }
        return view
    }

    override fun onDestroy() {
        super.onDestroy(); _binding = null
    }

    override fun onPurchaseSuccess() {}
    override fun onPurchaseFailure() {}
}
