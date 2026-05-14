package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.poseSelection

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.AdapterPose
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWithRewardedGate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.runWhenRewardedAdClosed
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.RetrofitClient
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleAdult
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleChild
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getFemaleTeen
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleAdult
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleChild
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.getMaleTeen
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentSelectPoseBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini.GeminiViewModel
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini.ImageViewModelFactory
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

class SelectPoseFragment : Fragment() {

    private val viewModel1: GeminiViewModel by viewModels { ImageViewModelFactory() }
    private var _binding: FragmentSelectPoseBinding? = null
    private val binding get() = _binding!!

    private val args: SelectPoseFragmentArgs by navArgs()

    private val repository by lazy { ImageRepository(RetrofitClient.api) }
    private val viewModel: ImageViewModel by lazy { ImageViewModel(repository) }

    lateinit var optionAdapter: AdapterPose

    private var selectedChip: TextView? = null
    private var ageText = ""
    private var genderText = "Female"

    private var firstBitmap: Bitmap? = null
    private var faceImage: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectPoseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "SelectPoseFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "SelectPoseFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        if (PrefUtil.isPremium(requireContext())) {
            binding.watchImg.visibility = View.INVISIBLE
            binding.watchAd.visibility = View.INVISIBLE
        }

        setupToolbar()
        attachFirstImage()

        addGenderOptions(listOf("Female", "Male"))
        listOf(
            "Adult",
            "Teenage",
            "Child"
        ).forEach { binding.ageContainer.addView(createAgeChip(it)) }

        binding.upload.setOnClickListener {
            findNavController().navigate(
                SelectPoseFragmentDirections.actionSelectPoseFragmentToGalleryFragment(
                    featureType = FeatureType.POSE_SELECTION,
                    imageSlot = ImageSlot.FIRST
                )
            )
        }

        binding.generate.setOnClickListener {
            when {
                firstBitmap == null -> Toast.makeText(
                    requireContext(),
                    getString(R.string.upload_image_first),
                    Toast.LENGTH_SHORT
                ).show()

                faceImage == null -> Toast.makeText(
                    requireContext(),
                    getString(R.string.select_pose),
                    Toast.LENGTH_SHORT
                ).show()

                else -> checkPremiumAndGenerate()
            }
        }

        optionAdapter = AdapterPose(getFemaleAdult()) { faceImage = it }
        binding.recycler.adapter = optionAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.imageView.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.titleTextView.text = getString(R.string.pose)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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
                        runCatching { requireContext().uriToBitmap(Uri.parse(uri)) }.getOrNull()
                    }
                }
            }

            resId != -1 -> {
                binding.addImg.visibility = View.GONE
                binding.emptyBg.visibility = View.GONE
                Glide.with(this).load(resId).centerCrop().into(binding.generatedImage)
                lifecycleScope.launch {
                    firstBitmap = withContext(Dispatchers.IO) {
                        runCatching {
                            val d = requireContext().getDrawable(resId) ?: return@runCatching null
                            val bmp = createBitmap(
                                d.intrinsicWidth.coerceAtLeast(512),
                                d.intrinsicHeight.coerceAtLeast(512)
                            )
                            android.graphics.Canvas(bmp)
                                .also { c -> d.setBounds(0, 0, c.width, c.height); d.draw(c) }
                            bmp
                        }.getOrNull()
                    }
                }
            }
        }
    }

    private fun checkPremiumAndGenerate() {
        var generationStarted = false
        runWithRewardedGate(
            screen = "SelectPoseFragmentScreen",
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

    // ── ONLY success block changed ─────────────────────────────

    private fun handleImageUri() {
        val target = firstBitmap ?: return
        val pose = imageViewToBitmap(faceImage!!) ?: return

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.generate.visibility = View.GONE

        GeminiImageService().changeFace(
            apiKey = SharePref.getString(Constants.new_version_key, ""),
            targetImage = target,
            faceImage = pose,
            prompt = "Replace the pose naturally",
        ) { result ->
            if (!isAdded) return@changeFace
            runWhenRewardedAdClosed {
                binding.loadingOverlay.visibility = View.GONE
                binding.generate.visibility = View.VISIBLE
                result
                    .onSuccess { bitmap ->
                        ResultHolder.beforeBitmap = target
                        ResultHolder.afterBitmap = bitmap
                        findNavController().navigate(R.id.action_selectPoseFragment_to_beforeAfterFragment)
                    }
                    .onFailure { it.printStackTrace() }
            }
        }
    }

    // ── Chip / gender logic — unchanged ───────────────────────

    private fun createAgeChip(text: String): TextView {
        val context = requireContext()
        return TextView(context).apply {
            this.text = text; setTextColor(Color.WHITE); textSize = 14f; gravity =
            Gravity.CENTER; setPadding(18, 15, 18, 15)
            background = androidx.core.content.ContextCompat.getDrawable(
                context,
                R.drawable.custom_text_view
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 16, 16, 0) }
            setOnClickListener {
                selectedChip?.background = androidx.core.content.ContextCompat.getDrawable(
                    context,
                    R.drawable.custom_text_view
                )
                background = androidx.core.content.ContextCompat.getDrawable(
                    context,
                    R.drawable.custom_selected_text_view
                )
                selectedChip = this; ageText = text
                if (::optionAdapter.isInitialized) updateRecyclerData(
                    optionAdapter,
                    genderText,
                    ageText
                )
            }
        }
    }

    private fun updateRecyclerData(adapter: AdapterPose, gender: String = "Female", age: String?) {
        Log.d("TAG", "updateRecyclerData: $gender , $age")
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

    private fun addGenderOptions(options: List<String>) {
        val container = binding.genderContainer
        options.forEach { option ->
            val isSelected = option == "Female"
            val optionView = createGenderOption(option, isSelected) { selectedView ->
                genderText = selectedView.tag as String
                if (::optionAdapter.isInitialized) updateRecyclerData(
                    optionAdapter,
                    genderText,
                    ageText
                )
                for (i in 0 until container.childCount) {
                    container.getChildAt(i).findViewById<ImageView>(R.id.checkIcon)
                        ?.setImageResource(R.drawable.ic_uncheck)
                }
                selectedView.findViewById<ImageView>(R.id.checkIcon)
                    ?.setImageResource(R.drawable.ic_check)
            }
            container.addView(optionView)
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
}
