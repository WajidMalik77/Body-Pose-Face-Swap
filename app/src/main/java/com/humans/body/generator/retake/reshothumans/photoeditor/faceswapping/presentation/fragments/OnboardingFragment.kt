package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.OnBoardingActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfUserIsSubscribed { isSubscribed ->

            if (!isAdded || view.context == null) {
                return@checkIfUserIsSubscribed
            }

            if (!isSubscribed) {
            }
        }

        binding.textView.setOnClickListener {
            startActivity(Intent(requireActivity(), OnBoardingActivity::class.java))
            requireActivity().finish()
        }


    }

    @SuppressLint("HardwareIds")
    private fun checkIfUserIsSubscribed(callback: (Boolean) -> Unit) {
        val deviceId =
            Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val subscriptionRef = FirebaseDatabase.getInstance()
            .getReference("subscriptions")
            .child(deviceId)

        subscriptionRef.get().addOnSuccessListener { snapshot ->
            val isSubscribed = snapshot.child("isSubscribed").getValue(Boolean::class.java) == true
            callback(isSubscribed)
        }.addOnFailureListener {
            callback(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}