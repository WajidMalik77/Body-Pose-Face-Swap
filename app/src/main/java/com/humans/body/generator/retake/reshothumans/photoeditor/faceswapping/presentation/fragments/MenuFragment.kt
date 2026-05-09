package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.LanguagesActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.SettAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentMenuBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getMenuData
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.openFeedback
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.openRateUs
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.privacyPolicy
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.shareApp

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMenuBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "MenuFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "MenuFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        binding.back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.recycler.adapter = SettAdapter(requireContext().getMenuData()) {
            when (it) {
                0 -> {
                    requireActivity().startActivity(
                        Intent(
                            requireContext(),
                            PremiumActivity::class.java
                        )
                    )
                }

                1 -> startActivity(Intent(requireContext(), LanguagesActivity::class.java))
                2 -> shareApp(requireContext())
                3 -> requireContext().openRateUs()
                4 -> requireContext().openFeedback()
                5 -> requireContext().privacyPolicy("https://pps66.wordpress.com/human-body-generator/")
                6 -> requireContext().privacyPolicy()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}
