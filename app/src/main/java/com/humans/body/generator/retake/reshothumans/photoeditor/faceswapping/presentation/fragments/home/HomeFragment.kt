package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentHomeBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.FunnelAnalytics

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshHomeAds()

        // ── Toolbar ───────────────────────────────────────────────
        binding.toolbar.GetPro.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "premium")
            requireActivity().startActivity(Intent(requireContext(), PremiumActivity::class.java))
        }
        binding.toolbar.imageView.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "settings")
            findNavController().navigate(R.id.menuFragment)
        }

        // ── Body Editing ──────────────────────────────────────────
        binding.layoutCardBodyReshape.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "body_reshape")
            navigateToGallery(FeatureType.BODY_RESHAPE)
        }
        binding.layoutCardPoseSelection.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "scan_now")
            navigateToGallery(FeatureType.POSE_SELECTION)
        }

        binding.layoutCardTextToImage.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "text_to_image")
            safeShowInterstitialNavigate("HomeFragmentScreen", "text_to_image") {
                findNavController().navigate(R.id.textToImageFragment)
            }
        }
        binding.layoutCardFaceSwap.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "face_swap")
            navigateToGallery(FeatureType.FACE_SWAP)
        }
        binding.layoutCardFaceStyle.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "face_style")
            navigateToGallery(FeatureType.FACE_STYLE)
        }

        // ── Advanced AI Editing ───────────────────────────────────
        binding.layoutCardAiTextToImage.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "image_to_image")
            navigateToGallery(FeatureType.IMAGE_EDITING)
        }
        binding.layoutCardAiFaceSwap.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "in_painting")
            navigateToGallery(FeatureType.IN_PAINTING)
        }
        binding.layoutCardAiFaceStyle.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "upscaling")
            navigateToGallery(FeatureType.UPSCALING)
        }
        binding.layoutCardAiResizing.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "ai_resizing")
            navigateToGallery(FeatureType.AI_RESIZING)
        }
        binding.layoutCardAiRemoveBg.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "remove_bg")
            navigateToGallery(FeatureType.REMOVE_BG)
        }
        binding.layoutCardAiImageVariation.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "image_variation")
            navigateToGallery(FeatureType.IMAGE_VARIATION)
        }
        binding.layoutCardAiSwapHairstyle.setOnClickListener {
            FunnelAnalytics.logHomeClick(requireContext(), "swap_hairstyle")
            navigateToGallery(FeatureType.SWAP_HAIRSTYLE)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHomeAds()
    }

    private fun refreshHomeAds() {
        if (!isAdded || _binding == null) return

        if (PrefUtil.isPremium(requireContext())) {
            hideAllHomeAds()
            return
        }

        binding.llBannerTop.isVisible = true
        binding.llNativeCenter.isVisible = true
        binding.llNativeBottom.isVisible = true

        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        if (bannerTopContainer.childCount == 0) {
            loadBannerAds(
                screen = "HomeFragmentScreen",
                topContainer = bannerTopContainer,
                topShimmer = bannerTopShimmer
            )
        }

        val nativeCenterContainer = binding.llNativeCenter.findViewById<FrameLayout>(R.id.admob_native)
        val nativeCenterShimmer =
            binding.llNativeCenter.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)

        val centerNeedsLoad = nativeCenterContainer.childCount == 0
        val bottomNeedsLoad = nativeBottomContainer.childCount == 0
        if (centerNeedsLoad || bottomNeedsLoad) {
            loadNativeAds(
                screen = "HomeFragmentScreen",
                centerContainer = if (centerNeedsLoad) nativeCenterContainer else null,
                centerShimmer = if (centerNeedsLoad) nativeCenterShimmer else null,
                bottomContainer = if (bottomNeedsLoad) nativeBottomContainer else null,
                bottomShimmer = if (bottomNeedsLoad) nativeBottomShimmer else null
            )
        }
    }

    private fun hideAllHomeAds() {
        if (_binding == null) return

        binding.llBannerTop.visibility = View.GONE
        binding.llNativeCenter.visibility = View.GONE
        binding.llNativeBottom.visibility = View.GONE

        clearAdViews(binding.llBannerTop)
        clearAdViews(binding.llNativeCenter)
        clearAdViews(binding.llNativeBottom)
    }

    private fun clearAdViews(root: View) {
        root.findViewById<FrameLayout>(R.id.admob_banner)?.removeAllViews()
        root.findViewById<FrameLayout>(R.id.admob_native)?.removeAllViews()

        root.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)?.apply {
            stopShimmer()
            visibility = View.GONE
        }
        root.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)?.apply {
            stopShimmer()
            visibility = View.GONE
        }
    }

    private fun navigateToGallery(type: FeatureType) {
        safeShowInterstitialNavigate("HomeFragmentScreen", "open_${type.name.lowercase()}") {
            val action = HomeFragmentDirections
                .actionHomeFragmentToGalleryFragment(
                    featureType = type,
                    imageSlot = ImageSlot.FIRST
                )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
