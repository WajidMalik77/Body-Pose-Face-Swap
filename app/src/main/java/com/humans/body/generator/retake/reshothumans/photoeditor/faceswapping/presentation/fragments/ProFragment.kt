package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.CarouselAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentProBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.SubscriptionPurchaseInterface
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.getCarouselList

class ProFragment : BottomSheetDialogFragment(), SubscriptionPurchaseInterface {
    private var _binding: FragmentProBinding? = null
    private val binding get() = _binding!!
    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    private lateinit var adapter: CarouselAdapter

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

    private var currentPage = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentProBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickListener {
            if (isAdded && isResumed) {
                dismissAllowingStateLoss()
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

    override fun productPurchasedSuccessful() {
        TODO("Not yet implemented")
    }

    override fun productPurchaseFailed() {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}