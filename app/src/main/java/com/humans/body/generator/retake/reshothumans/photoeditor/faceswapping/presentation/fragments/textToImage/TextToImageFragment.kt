package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.textToImage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentTextToImageBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Constants
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SharePref
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageTagsMap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.navigateWithBitmap

class TextToImageFragment : Fragment() {
    private var _binding: FragmentTextToImageBinding? = null
    private val binding get() = _binding!!
    private val selectedChipsMap = mutableMapOf<String, String>()
    private var selectionDialog: AlertDialog? = null
    private val geminiImageService by lazy { GeminiImageService() }
    private var gateSatisfied = false
    private var generationStarted = false
    private var pendingGeneratedBitmap: android.graphics.Bitmap? = null
    private var pendingGenerationError: String? = null
    private var activeGateRequestId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextToImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "TextToImageFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "TextToImageFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        binding.toolbar.imageView.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.titleTextView.text = requireContext().getString(R.string.text_to_image)

        binding.ageSlider.setCustomThumbDrawable(R.drawable.thumb_custom)
        val defaultAge = binding.ageSlider.value.toInt()
        addKeywordChip("age", "Age: $defaultAge")
        binding.ageSlider.addOnChangeListener { _, value, _ ->
            addKeywordChip("age", "Age: ${value.toInt()}")
        }

        binding.generate.setOnClickListener { handleGenerateClick() }

        val genderOptions = listOf("Female", "Male", "Non Binary")
        addGenderOptions(genderOptions)

        binding.reset.setOnClickListener {
            clearAllKeywordChips()
        }

        val items = listOf("Style", "Color", "Top", "Bottom", "Underwear", "Outwear", "Footwear", "Accessories")
        val ages = listOf("Skin tone", "Ethnicity", "Body Type")
        val optionsItem = listOf("HairStyle", "HairColor", "Background")

        ages.forEach { binding.ageContainer.addView(createChip(it)) }
        items.forEach { binding.tagContainer.addView(createChip(it)) }
        optionsItem.forEach { binding.optionsContainer.addView(createChip(it)) }

        binding.randomBtn.setOnClickListener {
            val randomEntry = imageTagsMap.entries.random()
            val randomTags = randomEntry.value
            binding.keywordContainer.removeAllViews()
            selectedChipsMap.clear()
            randomTags.forEach { tag -> addKeywordChip(category = tag, text = tag) }
            Toast.makeText(requireContext(), "Random values applied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("HardwareIds")
    private fun handleGenerateClick() {
        val prompt = getCombinedPrompt()
        if (prompt.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a prompt", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.scroll.visibility = View.GONE
        Log.d("TAG", "handleGenerateClick: $prompt")
        binding.genTxt.isEnabled = false

        gateSatisfied = false
        generationStarted = false
        pendingGeneratedBitmap = null
        pendingGenerationError = null
        activeGateRequestId += 1
        val requestId = activeGateRequestId

        runWithRewardedGate(
            screen = "TextToImageFragmentScreen",
            trigger = "generate",
            onAdShowing = {
                if (requestId == activeGateRequestId && !generationStarted) {
                    startGeminiGeneration(prompt, requestId)
                }
            },
            onBlocked = {
                if (requestId == activeGateRequestId) {
                    resetGenerationUiState(invalidateRequest = true)
                }
            }
        ) {
            if (requestId != activeGateRequestId) return@runWithRewardedGate
            gateSatisfied = true
            if (!generationStarted) {
                startGeminiGeneration(prompt, requestId)
            } else {
                pendingGeneratedBitmap?.let {
                    completeGenerationSuccess(it, requestId)
                }
                pendingGenerationError?.let {
                    completeGenerationFailure(it, requestId)
                }
            }
        }
    }

    private fun resetGenerationUiState(invalidateRequest: Boolean = false) {
        gateSatisfied = false
        generationStarted = false
        pendingGeneratedBitmap = null
        pendingGenerationError = null
        if (invalidateRequest) {
            activeGateRequestId += 1
        }
        if (_binding != null) {
            binding.genTxt.isEnabled = true
            binding.progress.visibility = View.GONE
            binding.scroll.visibility = View.VISIBLE
        }
    }

    private fun startGeminiGeneration(prompt: String, requestId: Int) {
        if (requestId != activeGateRequestId) return
        generationStarted = true
        geminiImageService.generateTextToImageWithGemini(
            apiKey = SharePref.getString(Constants.new_version_key, "").trim(),
            prompt = prompt
        ) { result ->
            if (!isAdded || requestId != activeGateRequestId) return@generateTextToImageWithGemini
            result.onSuccess { resource ->
                if (gateSatisfied) {
                    completeGenerationSuccess(resource, requestId)
                } else {
                    pendingGeneratedBitmap = resource
                }
            }.onFailure { error ->
                val msg = error.message ?: "Error occurred"
                if (gateSatisfied) {
                    completeGenerationFailure(msg, requestId)
                } else {
                    pendingGenerationError = msg
                }
            }
        }
    }

    private fun completeGenerationSuccess(resource: android.graphics.Bitmap, requestId: Int) {
        if (!isAdded || requestId != activeGateRequestId) return
        pendingGeneratedBitmap = null
        pendingGenerationError = null
        runWhenRewardedAdClosed {
            if (requestId != activeGateRequestId) return@runWhenRewardedAdClosed
            binding.genTxt.isEnabled = true
            navigateWithBitmap(resource, R.id.action_textToImageFragment_to_generatePictureFragment)
            binding.progress.visibility = View.GONE
            binding.scroll.visibility = View.VISIBLE
        }
    }

    private fun completeGenerationFailure(message: String, requestId: Int) {
        if (!isAdded || requestId != activeGateRequestId) return
        runWhenRewardedAdClosed {
            if (requestId != activeGateRequestId) return@runWhenRewardedAdClosed
            resetGenerationUiState()
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCombinedPrompt(): String {
        val keywords = mutableListOf<String>()
        for (i in 0 until binding.keywordContainer.childCount) {
            val view = binding.keywordContainer.getChildAt(i)
            if (view is TextView) keywords.add(view.text.toString().trim())
        }
        return keywords.joinToString(", ")
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
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

    private fun clearAllKeywordChips() {
        binding.keywordContainer.removeAllViews()
        selectedChipsMap.clear()
    }

    private fun createChip(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(34, 18, 34, 18)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_text_view)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, 16, 16, 0)
            this.layoutParams = layoutParams
            setOnClickListener {
                when (text) {
                    "Skin tone" -> showCustomRadioDialog("Skin Tone", listOf("Porcelain", "Rosy", "Peach", "White", "Neutral", "Golden", "Olive", "Tan", "Caramel", "Bronze", "Mahogany", "Dark Mahogany", "Cocoa", "Dark", "Onyx Black", "Black Purple", "Ethnicity", "Clear", "Albanian")) {}
                    "Ethnicity" -> showCustomRadioDialog("Ethnicity", listOf("African", "African American", "Albanian", "Algerian", "American", "Argentine", "Arab", "Armenian", "Australian", "Austrian", "Azerbaijani", "Bangladeshi", "Bahraini", "Belarusian", "Belgian", "Bolivian", "Brazilian", "British", "Bulgarian", "Canadian", "Caucasian", "Chilean", "Chinese", "Colombian", "Costa Rican", "Croatian", "Cuban", "Czech", "Danish", "Dominican", "Dutch", "Ecuadorian", "Egyptian", "Emirati", "Estonian", "Ethiopian", "Filipino", "Finnish", "French", "Georgian", "German", "Ghanaian", "Greek", "Guatemalan", "Honduran", "Hispanic", "Hungarian", "Icelandic", "Indian", "Indonesian", "Iranian", "Iraqi", "Irish", "Israeli", "Italian", "Jamaican", "Japanese", "Jordanian", "Kenyan", "Korean", "Kuwaiti", "Latvian", "Lebanese", "Libyan", "Lithuanian", "Macedonian", "Malaysian", "Malian", "Mexican", "Mongolian", "Moroccan", "Nepalese", "New Zealand", "Nicaraguan", "Nigerian", "Norwegian", "Omani", "Pakistani", "Panamanian", "Paraguayan", "Peruvian", "Polish", "Portuguese", "Puerto Rican", "Qatari", "Romanian", "Russian", "Saudi Arabian", "Senegalese", "Serbian", "Singaporean", "Slovak", "Slovenian", "Somali", "South African", "South Korean", "Spanish", "Sri Lankan", "Sudanese", "Swedish", "Swiss", "Syrian", "Taiwanese", "Tanzanian", "Thai", "Togolese", "Tunisian", "Turkish", "Ugandan", "Ukrainian", "Uruguayan", "Uzbek", "Venezuelan", "Vietnamese", "Yemeni", "Zambian", "Zimbabwean")) {}
                    "Body Type" -> showCustomRadioDialog("Body Type", listOf("Very Thin", "Thin", "Average", "Athletic", "Fit", "Curvy", "Overweight")) {}
                    "Style" -> showCustomRadioDialog("Style", listOf("formal", "casual", "bohemian", "vintage", "streetwear", "hipster", "athleisure", "preppy", "gothic", "punk", "minimalist", "tomboy", "retro", "glamorous", "edgy", "classic", "chic", "romantic", "sporty", "artsy", "ethnic", "business", "casual chic", "high fashion", "rock", "country", "rugged", "avant garde", "elegant", "boho-chic")) {}
                    "Color" -> showCustomRadioDialog("Color", listOf("Black", "White", "Gray", "Navy", "Blue", "Teal", "Green", "Olive", "Yellow", "Orange", "Red", "Pink", "Purple", "Brown", "Beige", "Cream", "Burgundy", "Mustard", "Lavender", "Mint", "Coral", "Champagne", "Taupe", "Gold", "Rose Gold", "Neutral", "Pastel", "Earth Tone", "Cool Tones", "Warm Tones", "Muted", "Metallic")) {}
                    "Top" -> showCustomRadioDialog("Top", listOf("T-shirt", "Blouse", "Shirt", "Tank top", "Dress", "Mini dress", "Midi dress", "Maxi dress", "Shirt dress", "Sweater", "Hoodie", "Jumpsuit")) {}
                    "Bottom" -> showCustomRadioDialog("Bottom", listOf("Jeans", "Pants", "Wide-Leg Pants", "Cargo Pants", "Flared Pants", "Skirt", "Mini Skirt", "Midi Skirt", "Maxi Skirt", "Pencil Skirt", "Pleated Skirt", "Shorts", "High-Waisted Shorts", "Leggings", "Trousers")) {}
                    "Underwear" -> showCustomRadioDialog("Underwear", listOf("Underwear", "Bikini", "Pajamas", "Thermal Underwear", "Swimsuit", "Camisole", "Negligee")) {}
                    "Outwear" -> showCustomRadioDialog("Outwear", listOf("Pea coat", "Trench coat", "Jacket", "Suit jacket", "Denim jacket", "Bomber jacket", "Leather jacket", "Parka", "Cardigan")) {}
                    "Footwear" -> showCustomRadioDialog("Footwear", listOf("Sneakers", "Boots", "Sandals", "Heels", "Flats", "Shoes", "Loafers", "Oxfords", "Ballet Flats", "Platform Shoes")) {}
                    "Accessories" -> showCustomRadioDialog("Accessories", listOf("Hat", "Belt", "Scarf", "Necklace", "Earrings", "Glasses", "Sunglasses", "Handbag", "Headband")) {}
                    "HairStyle" -> showCustomRadioDialog("HairStyle", listOf("Long hair", "Medium hair", "Short hair", "Curly", "Wavy", "Bald", "Bob haircut", "Shag haircut", "Bangs", "Ponytail", "Undercut", "Top knot", "French braid", "Messy bun", "Updo", "Mohawk", "Shaved head", "Pompadour", "Afro hair", "Dreadlocks", "Braids")) {}
                    "HairColor" -> showCustomRadioDialog("HairColor", listOf("Black", "Brown", "Blonde", "Ginger", "Gray", "White")) {}
                    "Background" -> showCustomRadioDialog("Background", listOf("White wall", "Studio backdrop", "Brick wall", "Concrete wall", "Floral wallpaper", "Forest", "Cityscape", "Industrial area", "Rooftop", "Graffiti wall", "Minimalist setting", "Nature landscape", "Urban alley", "Botanical garden", "Old staircase", "Library", "Desert", "Countryside", "Waterfall", "Mountain range", "Park", "Abandoned building", "Shop", "Rustic barn", "Warehouse", "Sunset", "Sunrise", "Architecture")) {}
                }
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

    private fun addGenderOptions(options: List<String>) {
        val container = binding.genderContainer
        options.forEach { option ->
            val isSelected = option == "Female"
            val optionView = createGenderOption(option, isSelected) { selectedView ->
                val selectedText = selectedView.tag as String
                val existingChip = binding.keywordContainer.children.firstOrNull { it is TextView && it.text == selectedText }
                if (existingChip != null) return@createGenderOption
                addKeywordChip("Gender", selectedText)
                for (i in 0 until container.childCount) {
                    container.getChildAt(i).findViewById<ImageView>(R.id.checkIcon)?.setImageResource(
                        R.drawable.ic_uncheck)
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
}
