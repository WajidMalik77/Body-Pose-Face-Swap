package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.PremiumActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.safeShowInterstitialNavigate
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.MixedAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.RetrofitClient
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentDashboardBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.repositories.ImageRepository
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_FIRST
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_FIRST_TWO
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_FOUR_VERTICAL
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_LAST_TWO
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_TITLE
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getHomeData
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.navigateWithBitmap
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.viewmodels.ImageViewModel

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val repository by lazy { ImageRepository(RetrofitClient.api) }
    private val viewModel: ImageViewModel by lazy { ImageViewModel(repository) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "DashboardFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "DashboardFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        binding.toolbar.GetPro.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), PremiumActivity::class.java))

        }
        binding.toolbar.imageView.setOnClickListener {
//            navController.navigate(R.id.menuFragment)
            findNavController().navigate(R.id.menuFragment)

        }

        val layoutManager = GridLayoutManager(requireContext(), 4)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (requireContext().getHomeData()[position].type) {
                    TYPE_FIRST -> 4
                    TYPE_FIRST_TWO -> 2
                    TYPE_FOUR_VERTICAL -> 1
                    TYPE_TITLE -> 4
                    TYPE_LAST_TWO -> 2
                    else -> 1
                }
            }
        }
        binding.recycler.layoutManager = layoutManager
        val adapter = MixedAdapter { pos, bitmap ->
            if (pos == 14) return@MixedAdapter

            when (pos) {
                0 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "reshape") { findNavController().navigate(R.id.reshapeFragment) }
                1 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "select_pose") { findNavController().navigate(R.id.selectPoseFragment) }
                2 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "face_swap") { findNavController().navigate(R.id.faceSwapFragment) }
                3 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "face_style") { findNavController().navigate(R.id.faceStyleFragment) }
                4 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "photo_editor") { findNavController().navigate(R.id.photoEditorFragment) }
                5 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "text_to_image") { findNavController().navigate(R.id.textToImageFragment) }
                6 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "image_to_image") { findNavController().navigate(R.id.imageToImageFragment) }
                7 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "camera") { findNavController().navigate(R.id.cameraFragment) }
                8 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "re_style") { findNavController().navigate(R.id.reStyleFragment) }
                9 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "image_variation") { findNavController().navigate(R.id.imageVariationFragment) }
                10 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "ai_resizer") { findNavController().navigate(R.id.aiResizerFragment) }
                11 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "crop") { findNavController().navigate(R.id.cropFragment) }
                12 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "bg_remover") { findNavController().navigate(R.id.bgRemoverFragment) }
                13 -> safeShowInterstitialNavigate("DashboardFragmentScreen", "up_scale") { findNavController().navigate(R.id.upScaleFragment) }
                14 -> {}
                else -> {
                    Log.d("TemplatesFragment", "onViewCreated: $bitmap")

                    safeShowInterstitialNavigate("DashboardFragmentScreen", "open_templates") {
                        navigateWithBitmap(
                            bitmap!!,
                            R.id.action_dashboardFragment_to_templatesFragment
                        )
                    }

                }
            }

        }

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            binding.recycler.adapter = adapter
        }

        viewModel.loadData(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}
