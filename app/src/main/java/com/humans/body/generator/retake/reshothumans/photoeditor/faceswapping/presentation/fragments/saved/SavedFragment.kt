package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.saved

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentSavedBinding
import java.io.File
import java.io.FileOutputStream

class SavedFragment : Fragment() {
    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private var sharedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer =
            binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "SavedFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "SavedFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )

        setupToolbar()
        setupSubtitleSpan()
        loadImage()
        setupShareButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Toolbar ────────────────────────────────────────────────
    private fun setupToolbar() {
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }
        binding.saveBtn.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    // ── "Share" word colored purple ────────────────────────────
    private fun setupSubtitleSpan() {
        val full = "Share Your HD Photo with the World"
        val spannable = SpannableString(full)
        spannable.setSpan(
            ForegroundColorSpan(requireContext().getColor(R.color.purple_share)),
            0, 5,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textView1.text = spannable
    }

    // ── Load image from args ───────────────────────────────────
    private fun loadImage() {
        val uriString = arguments?.getString("IMAGE_URI") ?: return
        val uri = Uri.parse(uriString)
        val bitmap = runCatching {
            BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
        }.getOrNull() ?: return
        binding.generatedImage.setImageBitmap(bitmap)
        sharedImageUri = bitmapToShareUri(bitmap)
    }

    // ── Share buttons ──────────────────────────────────────────
    private fun setupShareButtons() {
        binding.btnWhatsapp.setOnClickListener { shareToApp("com.whatsapp") }
        binding.btnFacebook.setOnClickListener { shareToApp("com.facebook.katana") }
        binding.btnInstagram.setOnClickListener { shareToApp("com.instagram.android") }
        binding.btnShare.setOnClickListener { shareGeneral() }
    }

    private fun shareToApp(packageName: String) {
        val uri = sharedImageUri ?: run {
            Toast.makeText(requireContext(), "Image not ready", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out my AI-generated photo!")
            setPackage(packageName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (requireContext().packageManager.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun shareGeneral() {
        val uri = sharedImageUri ?: run {
            Toast.makeText(requireContext(), "Image not ready", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Check out my AI-generated photo!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "Share via"
            )
        )
    }
    private fun bitmapToShareUri(bitmap: Bitmap): Uri? = runCatching {
        val cacheDir = File(requireContext().cacheDir, "shared_images").also { it.mkdirs() }
        val file = File(cacheDir, "share_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
    }.getOrNull()
}
