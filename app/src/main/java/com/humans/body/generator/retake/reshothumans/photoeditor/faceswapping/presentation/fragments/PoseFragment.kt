package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.BillingManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.RetrofitClient
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentPoseBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.repositories.ImageRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TrialManager
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.viewmodels.ImageViewModel

class PoseFragment : Fragment(), BillingManager.PurchaseListener {
    private var _binding: FragmentPoseBinding? = null
    private val binding get() = _binding!!
    private val selectedChipsMap = mutableMapOf<String, String>()
    private var selectionDialog: AlertDialog? = null
    private var selectedChip: TextView? = null
    private var selectedPoseChip: TextView? = null
    private var selectedTargetImageView: ImageView? = null
    private var selectedTargetFrameImageView: ImageView? = null
    private var isGenerating = false
    private val repository by lazy { ImageRepository(RetrofitClient.api) }
    private val viewModel: ImageViewModel by lazy { ImageViewModel(repository) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPoseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "PoseFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "PoseFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        selectedTargetImageView = binding.imageView5
        selectedTargetFrameImageView = binding.imageView6

        val genderOptions = listOf("Female", "Male")
        addGenderOptions(genderOptions)

        binding.generate.setOnClickListener {
            if (isGenerating) return@setOnClickListener
            handleGenerateClick()
        }

        val optionsItem = listOf("Emotion", "Skin Tone", "Hair Color", "Hair Length", "Glasses", "Makeup")
        optionsItem.forEach { binding.optionsContainer.addView(createChip(it)) }

        val ages = listOf("Adult", "Young adult", "Child", "Middle-aged", "Infant")
        ages.forEach { binding.ageContainer.addView(createAgeChip(it)) }

        val pose = listOf("Center", "Right", "Left", "Up", "Down")
        pose.forEach { binding.poseContainer.addView(createPoseChip(it)) }
    }

    private fun handleGenerateClick() {
        runWithRewardedGate("PoseFragmentScreen", "generate") {
            generateImage()
        }
    }

    private fun generateImage() {
        isGenerating = true
        binding.GenTxt.isEnabled = false

        viewModel.generateImage(
            SharePref.getString(Constants.new_version_key, ""),
            getCombinedPrompt(),
            onSuccess = { response ->
                binding.GenTxt.isEnabled = true
                isGenerating = false
                safeShowInterstitialNavigate("PoseFragmentScreen", "generate") {
                    val action = PoseFragmentDirections
                        .actionPoseFragmentToGeneratePictureFragment(response.url ?: "")
                    findNavController().navigate(action)
                }
            },
            onFailure = { errorMsg ->
                binding.GenTxt.isEnabled = true
                isGenerating = false
                Toast.makeText(requireContext(), errorMsg ?: "Error occurred", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun getCombinedPrompt(): String {
        val keywords = mutableListOf<String>()
        for (i in 0 until binding.keywordContainer.childCount) {
            val view = binding.keywordContainer.getChildAt(i)
            if (view is TextView) {
                keywords.add(view.text.toString().trim())
            }
        }
        return keywords.joinToString(", ")
    }

    private fun createChip(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(18, 15, 18, 15)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_text_view)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 16, 0)
            this.layoutParams = layoutParams
            setOnClickListener {
                when (text) {
                    "Emotion" -> showCustomRadioDialog("Emotion", listOf("Natural", "Happy", "Surprised", "Angry", "Contemptuous", "Disgusted", "Frightened", "Sad")) {}
                    "Skin Tone" -> showCustomRadioDialog("Skin Tone", listOf("Porcelain", "Rosy", "Peach", "White", "Neutral", "Golden", "Olive", "Tan", "Caramel", "Bronze", "Mahogany", "Dark Mahogany", "Cocoa", "Dark", "Onyx Black", "Black Purple", "Ethnicity", "Clear", "Albanian")) {}
                    "Hair Color" -> showCustomRadioDialog("Hair Color", listOf("Black", "Brown", "Blonde", "Ginger", "Gray", "White")) {}
                    "Hair Length" -> showCustomRadioDialog("Hair Length", listOf("Short", "Medium", "Large")) {}
                    "Glasses" -> showCustomRadioDialog("Glasses", listOf("Reading Glasses", "Sunglasses")) {}
                    "Makeup" -> showCustomRadioDialog("Makeup", listOf("Eyes", "Lips")) {}
                }
            }
        }
    }

    private fun createAgeChip(text: String): TextView {
        val context = requireContext()
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(18, 15, 18, 15)
            background = ContextCompat.getDrawable(context, R.drawable.custom_text_view)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 16, 0)
            this.layoutParams = layoutParams
            setOnClickListener {
                selectedChip?.background = ContextCompat.getDrawable(context, R.drawable.custom_text_view)
                background = ContextCompat.getDrawable(context, R.drawable.custom_selected_text_view)
                selectedChip = this
                addKeywordChip("Age", text)
            }
        }
    }

    private fun createPoseChip(text: String): TextView {
        val context = requireContext()
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(18, 15, 18, 15)
            background = ContextCompat.getDrawable(context, R.drawable.custom_text_view)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 16, 0)
            this.layoutParams = layoutParams
            setOnClickListener {
                selectedPoseChip?.background = ContextCompat.getDrawable(context, R.drawable.custom_text_view)
                background = ContextCompat.getDrawable(context, R.drawable.custom_selected_text_view)
                selectedPoseChip = this
                addKeywordChip("Pose", text)
                selectedTargetImageView?.setImageResource(when (text) {
                    "Center" -> R.drawable.ic_center_pose
                    "Right" -> R.drawable.ic_right_pose
                    "Left" -> R.drawable.ic_left_pose
                    "Up" -> R.drawable.ic_up_pose
                    "Down" -> R.drawable.ic_down_pose
                    else -> R.drawable.ic_center_pose
                })
                selectedTargetFrameImageView?.setImageResource(when (text) {
                    "Center" -> R.drawable.ic_center_pose_frame
                    "Right" -> R.drawable.ic_right_pose_frame
                    "Left" -> R.drawable.ic_left_pose_frame
                    "Up" -> R.drawable.ic_up_pose_frame
                    "Down" -> R.drawable.ic_down_pose_frame
                    else -> R.drawable.ic_center_pose_frame
                })
            }
        }
    }

    private fun showCustomRadioDialog(category: String, options: List<String>, onSelected: (String?) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choice, null)
        val titleView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val clearPromptView = dialogView.findViewById<TextView>(R.id.clear_prompt)
        val container = dialogView.findViewById<LinearLayout>(R.id.optionContainer)

        titleView.text = category
        titleView.setTextColor(Color.WHITE)

        var selectedLayout: LinearLayout? = null
        var selectedOption: String? = null

        options.forEach { option ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(8, 8, 8, 8)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val textView = TextView(requireContext()).apply {
                text = option
                setTextColor(Color.WHITE)
                textSize = 16f
                setPadding(5, 5, 0, 5)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val imageView = ImageView(requireContext()).apply {
                id = R.id.radioIcon
                setImageResource(R.drawable.ic_radio_ellipse)
                layoutParams = LinearLayout.LayoutParams(48, 48)
            }
            itemLayout.addView(textView)
            itemLayout.addView(imageView)
            itemLayout.setOnClickListener {
                (selectedLayout?.getChildAt(1) as? ImageView)?.setImageResource(R.drawable.ic_radio_ellipse)
                imageView.setImageResource(R.drawable.ic_check)
                selectedLayout = itemLayout
                selectedOption = option
                onSelected(option)
                addKeywordChip(category, option)
                selectionDialog?.dismiss()
            }
            container.addView(itemLayout)
        }

        selectionDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        clearPromptView.setOnClickListener {
            (selectedLayout?.findViewById<ImageView>(R.id.radioIcon))?.setImageResource(R.drawable.ic_radio_ellipse)
            selectedLayout = null
            selectedOption = null
            onSelected(null)
            binding.keywordContainer.children.firstOrNull { it is TextView && it.tag == category }?.let {
                binding.keywordContainer.removeView(it)
            }
            selectedChipsMap.remove(category)
            selectionDialog?.dismiss()
        }
        selectionDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        selectionDialog?.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addKeywordChip(category: String, text: String) {
        val existingChip = binding.keywordContainer.children.firstOrNull {
            it is TextView && it.tag == category
        } as? TextView

        if (existingChip != null) {
            existingChip.text = text
            selectedChipsMap[category] = text
            return
        }

        val chip = TextView(requireContext()).apply {
            this.text = text
            this.tag = category
            setPadding(12, 12, 12, 12)
            setTextColor(Color.WHITE)
            textSize = 14f
            background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_prompt_view)
            isClickable = true
            isFocusable = true
            setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.ic_cross), null)
            compoundDrawablePadding = 12
            val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(10, 10, 10, 10)
            layoutParams = params
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = compoundDrawables[2]
                    if (drawableEnd != null) {
                        val drawableStart = width - paddingEnd - drawableEnd.bounds.width()
                        if (event.x >= drawableStart) {
                            binding.keywordContainer.removeView(this)
                            selectedChipsMap.remove(category)
                            performClick()
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
        }

        binding.keywordContainer.addView(chip)
        selectedChipsMap[category] = text
        (binding.keywordContainer.parent as? HorizontalScrollView)?.post {
            (binding.keywordContainer.parent as HorizontalScrollView).smoothScrollTo(chip.right, 0)
        }
    }

    private fun addGenderOptions(options: List<String>) {
        val container = binding.genderContainer
        options.forEach { option ->
            val isSelected = option == "Female"
            val optionView = createGenderOption(option, isSelected) { selectedView ->
                val selectedText = selectedView.tag as String
                val existingChip = binding.keywordContainer.children.firstOrNull {
                    it is TextView && it.text == selectedText
                }
                if (existingChip != null) return@createGenderOption
                addKeywordChip("Gender", selectedText)
                for (i in 0 until container.childCount) {
                    container.getChildAt(i).findViewById<ImageView>(R.id.checkIcon)?.setImageResource(R.drawable.ic_uncheck)
                }
                selectedView.findViewById<ImageView>(R.id.checkIcon)?.setImageResource(R.drawable.ic_check)
            }
            container.addView(optionView)
            if (isSelected) addKeywordChip("Gender", option)
        }
    }

    private fun createGenderOption(gender: String, isSelected: Boolean = false, onSelected: (View) -> Unit): View {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_gender_option, null)
        val label = view.findViewById<TextView>(R.id.label)
        val icon = view.findViewById<ImageView>(R.id.checkIcon)
        label.text = gender
        icon.setImageResource(if (isSelected) R.drawable.ic_check else R.drawable.ic_uncheck)
        view.tag = gender
        view.setOnClickListener { (it.tag as? String)?.let { _ -> onSelected(it) } }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onPurchaseSuccess() {}
    override fun onPurchaseFailure() {}
}
