package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "HomeFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeCenterContainer = binding.llNativeCenter.findViewById<FrameLayout>(R.id.admob_native)
        val nativeCenterShimmer =
            binding.llNativeCenter.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "HomeFragmentScreen",
            centerContainer = nativeCenterContainer,
            centerShimmer = nativeCenterShimmer,
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        // ── Toolbar ───────────────────────────────────────────────
        binding.toolbar.GetPro.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), PremiumActivity::class.java))
        }
        binding.toolbar.imageView.setOnClickListener {
            findNavController().navigate(R.id.menuFragment)
        }

        // ── Body Editing ──────────────────────────────────────────
        binding.layoutCardBodyReshape.setOnClickListener {
            navigateToGallery(FeatureType.BODY_RESHAPE)
        }
        binding.layoutCardPoseSelection.setOnClickListener {
            navigateToGallery(FeatureType.POSE_SELECTION)
        }

        binding.layoutCardTextToImage.setOnClickListener {
            safeShowInterstitialNavigate("HomeFragmentScreen", "text_to_image") {
                findNavController().navigate(R.id.textToImageFragment)
            }
        }
        binding.layoutCardFaceSwap.setOnClickListener {
            navigateToGallery(FeatureType.FACE_SWAP)
        }
        binding.layoutCardFaceStyle.setOnClickListener {
            navigateToGallery(FeatureType.FACE_STYLE)
        }

        // ── Advanced AI Editing ───────────────────────────────────
        binding.layoutCardAiTextToImage.setOnClickListener {
            navigateToGallery(FeatureType.IMAGE_EDITING)
        }
        binding.layoutCardAiFaceSwap.setOnClickListener {
            navigateToGallery(FeatureType.IN_PAINTING)
        }
        binding.layoutCardAiFaceStyle.setOnClickListener {
            navigateToGallery(FeatureType.UPSCALING)
        }
        binding.layoutCardAiResizing.setOnClickListener {
            navigateToGallery(FeatureType.AI_RESIZING)
        }
        binding.layoutCardAiRemoveBg.setOnClickListener {
            navigateToGallery(FeatureType.REMOVE_BG)
        }
        binding.layoutCardAiImageVariation.setOnClickListener {
            navigateToGallery(FeatureType.IMAGE_VARIATION)
        }
        binding.layoutCardAiSwapHairstyle.setOnClickListener {
            navigateToGallery(FeatureType.SWAP_HAIRSTYLE)
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
