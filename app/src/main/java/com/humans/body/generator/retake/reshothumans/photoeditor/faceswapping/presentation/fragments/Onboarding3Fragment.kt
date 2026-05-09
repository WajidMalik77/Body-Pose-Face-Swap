package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities.HomeActivity
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentOnboarding3Binding

class Onboarding3Fragment : Fragment() {
    private var _binding: FragmentOnboarding3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboarding3Binding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfUserIsSubscribed { isSubscribed ->
            if (!isSubscribed) {
            }
        }

        binding.textView.setOnClickListener {
            val intent = Intent(requireActivity(), HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun checkIfUserIsSubscribed(callback: (Boolean) -> Unit) {
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
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