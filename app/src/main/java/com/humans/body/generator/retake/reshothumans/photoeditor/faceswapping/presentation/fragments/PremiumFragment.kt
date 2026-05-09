package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.CarouselAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentPremiumBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SubscriptionPurchaseInterface
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getCarouselList


class PremiumFragment : Fragment(), SubscriptionPurchaseInterface {
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CarouselAdapter
    var check: String = ""

    private var currentPage = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.closeBtn.visibility = View.VISIBLE
        }, 1400)
        binding.closeBtn.setOnClickListener {
            if (isAdded && isResumed) {
                findNavController().popBackStack()
            }
        }
        adapter = CarouselAdapter(requireContext().getCarouselList())
        binding.viewPager.adapter = adapter
        binding.intoTabLayout.setupIndicators(adapter.itemCount)

        // Set the first indicator as active
        binding.intoTabLayout.selectIndicator(0)
        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPage = position
                    binding.intoTabLayout.selectIndicator(position)

                }
            }
        )

    }
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (isAdded && isResumed) {
                if (adapter.itemCount == 0) return

                currentPage = (currentPage + 1) % adapter.itemCount
                binding.viewPager.setCurrentItem(currentPage, true)

                handler.postDelayed(this, 3000) // 3 seconds
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded)
            handler.postDelayed(runnable, 3000)
    }

    override fun onPause() {
        super.onPause()
        if (isAdded)
            handler.removeCallbacks(runnable)
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun productPurchasedSuccessful() {
        TODO("Not yet implemented")
    }

    override fun productPurchaseFailed() {
        TODO("Not yet implemented")
    }

}