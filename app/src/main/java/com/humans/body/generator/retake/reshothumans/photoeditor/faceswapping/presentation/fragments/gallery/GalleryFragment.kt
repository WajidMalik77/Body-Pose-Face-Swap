package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.gallery

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.GalleryAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.RecentsAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters.TemplateAdapter
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadBannerAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.ads.helpers.loadNativeAds
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentGalleryBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.FeatureType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.presentation.fragments.types.ImageSlot

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val args: GalleryFragmentArgs by navArgs()

    private var selectedUri: String? = null
    private var selectedResId: Int? = null

    // ── Cached so MediaStore is only queried ONCE ──────────────
    private var cachedGalleryUris: List<String> = emptyList()

    private val templateDrawables: ArrayList<Int> = arrayListOf(
        R.drawable.temp_1,
        R.drawable.temp_2,
        R.drawable.temp_3,
        R.drawable.temp_4,
        R.drawable.temp_5,
        R.drawable.temp_6
    )

    // ── Launchers ──────────────────────────────────────────────

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                selectedUri = uri.toString()
                selectedResId = null
                (binding.rvTemplates.adapter as? TemplateAdapter)?.setSelected(emptySet())
                (binding.rvGallery.adapter as? GalleryAdapter)?.clearSelection()
                (binding.rvRecents.adapter as? RecentsAdapter)?.setSelected(null)
                syncDoneButton()
            }
        }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) loadGalleryImages()
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                .show()
        }

    // ── Lifecycle ──────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bannerTopContainer = binding.llBannerTop.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = binding.llBannerTop.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen = "GalleryFragmentScreen",
            topContainer = bannerTopContainer,
            topShimmer = bannerTopShimmer
        )

        val nativeBottomContainer = binding.llNativeBottom.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer =
            binding.llNativeBottom.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        loadNativeAds(
            screen = "GalleryFragmentScreen",
            bottomContainer = nativeBottomContainer,
            bottomShimmer = nativeBottomShimmer
        )
        setupToolbar()
        setupTemplatesRv()
        setupRecentsRv()
        checkPermissionAndLoadGallery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Toolbar ────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }
        binding.ivDone.setOnClickListener { onDoneClicked() }
    }

    // ── Done / route ───────────────────────────────────────────

    private fun onDoneClicked() {
        selectedUri?.let { GalleryPrefs.addRecent(requireContext(), it) }
        selectedResId?.let { GalleryPrefs.addRecent(requireContext(), it.toString()) }

        val uri = selectedUri
        val resId = selectedResId ?: -1

        when (args.imageSlot) {
            ImageSlot.FIRST -> {
                val dest = when (args.featureType) {
                    FeatureType.BODY_RESHAPE -> GalleryFragmentDirections.actionGalleryFragmentToReshapeFragment(
                        uri ?: "", resId
                    )
                    FeatureType.FACE_SWAP -> GalleryFragmentDirections.actionGalleryFragmentToFaceSwapFragment(
                        uri ?: "", resId
                    )
                    FeatureType.POSE_SELECTION -> GalleryFragmentDirections.actionGalleryFragmentToSelectPoseFragment(
                        uri ?: "", resId
                    )
                    FeatureType.TEXT_TO_IMAGE -> GalleryFragmentDirections.actionGalleryFragmentToTextToImageFragment(
                        uri ?: "", resId
                    )
                    FeatureType.FACE_STYLE -> GalleryFragmentDirections.actionGalleryFragmentToFaceStyleFragment(
                        uri ?: "", resId
                    )
                    FeatureType.IMAGE_EDITING -> GalleryFragmentDirections.actionGalleryFragmentToPhotoEditorFragment(
                        uri ?: "", resId
                    )
                    FeatureType.IN_PAINTING -> GalleryFragmentDirections.actionGalleryFragmentToReStyleFragment(
                        uri ?: "", resId
                    )
                    FeatureType.UPSCALING -> GalleryFragmentDirections.actionGalleryFragmentToUpScaleFragment(
                        uri ?: "", resId
                    )
                    FeatureType.AI_RESIZING -> GalleryFragmentDirections.actionGalleryFragmentToAiResizerFragment(
                        uri ?: "", resId
                    )
                    FeatureType.REMOVE_BG -> GalleryFragmentDirections.actionGalleryFragmentToBgRemoverFragment(
                        uri ?: "", resId
                    )
                    FeatureType.IMAGE_VARIATION -> GalleryFragmentDirections.actionGalleryFragmentToImageVariationFragment(
                        uri ?: "", resId
                    )
                    FeatureType.SWAP_HAIRSTYLE -> GalleryFragmentDirections.actionGalleryFragmentToFaceUploadFragment(
                        uri ?: "", resId
                    )
                }
                findNavController().navigate(dest)
            }

            // ── FIX: pass URI or resId separately so the observer can handle both ──
            ImageSlot.SECOND -> {
                val handle = findNavController().previousBackStackEntry?.savedStateHandle
                when {
                    uri != null -> handle?.set("secondImageUri", uri)
                    resId != -1 -> handle?.set("secondImageResId", resId)
                }
                findNavController().popBackStack()
            }
        }
    }

    // ── 1. Templates ───────────────────────────────────────────

    private fun setupTemplatesRv() {
        val size = calcSquareSize(HORIZONTAL_COLS)
        binding.rvTemplates.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = TemplateAdapter(
                templates = templateDrawables,
                itemSize = size,
                onTemplateClick = ::onTemplateSelected
            )
        }
    }

    private fun onTemplateSelected(resId: Int) {
        selectedResId = resId
        selectedUri = null
        (binding.rvTemplates.adapter as? TemplateAdapter)?.setSelected(setOf(resId))
        (binding.rvGallery.adapter as? GalleryAdapter)?.clearSelection()
        (binding.rvRecents.adapter as? RecentsAdapter)?.setSelected(resId.toString())
        syncDoneButton()
    }

    // ── 2. Recents ─────────────────────────────────────────────

    private fun setupRecentsRv() {
        val recents = GalleryPrefs.getRecents(requireContext())
        if (recents.isEmpty()) {
            binding.tvRecentsLabel.visibility = View.GONE
            binding.rvRecents.visibility = View.GONE
            return
        }
        binding.tvRecentsLabel.visibility = View.VISIBLE
        binding.rvRecents.visibility = View.VISIBLE
        val size = calcSquareSize(HORIZONTAL_COLS)
        binding.rvRecents.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = RecentsAdapter(
                rawItems = recents,
                itemSize = size,
                onRecentClick = ::onRecentSelected
            )
        }
    }

    // ── FIX: distinguish URI recents from resId recents ────────
    private fun onRecentSelected(raw: String) {
        val asResId = raw.toIntOrNull()
        (binding.rvRecents.adapter as? RecentsAdapter)?.setSelected(raw)
        if (asResId != null) {
            // This recent was saved from a drawable resource
            selectedResId = asResId
            selectedUri = null
            (binding.rvTemplates.adapter as? TemplateAdapter)?.setSelected(setOf(asResId))
            (binding.rvGallery.adapter as? GalleryAdapter)?.clearSelection()
            syncDoneButton()
        } else {
            // Normal gallery URI selected from Recents should not highlight Gallery list
            selectedUri = raw
            selectedResId = null
            (binding.rvTemplates.adapter as? TemplateAdapter)?.setSelected(emptySet())
            (binding.rvGallery.adapter as? GalleryAdapter)?.clearSelection()
            syncDoneButton()
        }
    }

    // ── 3. Gallery ─────────────────────────────────────────────

    private fun checkPermissionAndLoadGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadGalleryImages()
        } else {
            galleryPermissionLauncher.launch(permission)
        }
    }

    private fun loadGalleryImages() {
        cachedGalleryUris = queryMediaStore()
        val size = calcSquareSize(GALLERY_COLS)
        binding.rvGallery.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), GALLERY_COLS)
            adapter = GalleryAdapter(
                uris = cachedGalleryUris,
                itemSize = size,
                onCameraClick = ::openCamera,
                onImageClick = ::onGalleryImageSelected
            )
        }
    }

    private fun onGalleryImageSelected(uri: String) {
        val adapter = binding.rvGallery.adapter as? GalleryAdapter ?: return
        adapter.toggleSelection(uri)
        val nowSelected = adapter.getSelected()
        selectedUri = nowSelected.firstOrNull()
        selectedResId = null
        if (selectedUri != null) {
            (binding.rvTemplates.adapter as? TemplateAdapter)?.setSelected(emptySet())
        }
        (binding.rvRecents.adapter as? RecentsAdapter)?.setSelected(null)
        syncDoneButton()
    }

    // ── Camera ─────────────────────────────────────────────────

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = requireContext().packageManager

        if (cameraIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            cameraLauncher.launch(cameraIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    // ── MediaStore — called only once ──────────────────────────

    private fun queryMediaStore(): List<String> {
        val uris = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var count = 0
            while (cursor.moveToNext() && count < MAX_GALLERY_IMAGES) {
                uris.add(
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(col)
                    ).toString()
                )
                count++
            }
        }
        return uris
    }

    // ── UI helpers ─────────────────────────────────────────────

    private fun syncDoneButton() {
        binding.ivDone.visibility =
            if (selectedUri != null || selectedResId != null) View.VISIBLE else View.INVISIBLE
    }

    private fun calcSquareSize(columns: Int): Int {
        val hPad = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp) * 2
        val spacing =
            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp) * 2 * (columns - 1)
        return (resources.displayMetrics.widthPixels - hPad - spacing) / columns
    }

    // ── Constants ──────────────────────────────────────────────

    companion object {
        private const val GALLERY_COLS = 4
        private const val HORIZONTAL_COLS = 4
        private const val MAX_GALLERY_IMAGES = 500
    }

    // ── SharedPreferences ──────────────────────────────────────

    object GalleryPrefs {
        private const val PREF = "gallery_prefs"
        private const val KEY = "recents"
        private const val MAX = 16
        private const val SEP = "||"

        fun getRecents(context: Context): List<String> {
            val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY, "") ?: ""
            return if (raw.isBlank()) emptyList()
            else raw.split(SEP).filter { it.isNotBlank() }
        }

        fun addRecent(context: Context, key: String) {
            val updated = (listOf(key) + getRecents(context)).distinct().take(MAX)
            context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit { putString(KEY, updated.joinToString(SEP)) }
        }

        fun clear(context: Context) {
            context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit { remove(KEY) }
        }
    }
}
