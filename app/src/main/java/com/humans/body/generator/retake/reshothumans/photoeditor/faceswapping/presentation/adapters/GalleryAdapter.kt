package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryAdapter(
    private val uris: List<String>,
    private val itemSize: Int,
    selectedUris: Set<String> = emptySet(),
    private val onCameraClick: () -> Unit,
    private val onImageClick: (uri: String) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.VH>() {
    private val selected = selectedUris.toMutableSet()

    companion object {
        private const val TYPE_CAMERA = 0
        private const val TYPE_IMAGE = 1
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val ivCamera: ImageView = view.findViewById(R.id.ivCamera)
        val ivBadge: ImageView = view.findViewById(R.id.ivBadge)
    }

    override fun getItemViewType(position: Int) =
        if (position == 0) TYPE_CAMERA else TYPE_IMAGE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        view.layoutParams = RecyclerView.LayoutParams(itemSize, itemSize)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (getItemViewType(position)) {

            TYPE_CAMERA -> {
                Glide.with(holder.ivThumb).clear(holder.ivThumb)
                holder.ivThumb.setImageDrawable(null)
                holder.ivCamera.visibility = View.VISIBLE
                holder.ivBadge.visibility = View.GONE
                holder.itemView.setOnClickListener { onCameraClick() }
            }

            TYPE_IMAGE -> {
                val uri = uris[position - 1]
                holder.ivCamera.visibility = View.GONE
                holder.ivBadge.visibility =
                    if (selected.contains(uri)) View.VISIBLE else View.GONE

                Glide.with(holder.ivThumb)
                    .load(uri.toUri())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(itemSize, itemSize)
                    .centerCrop()
                    .into(holder.ivThumb)

                holder.itemView.setOnClickListener { onImageClick(uri) }
            }
        }
    }

    override fun getItemCount() = uris.size + 1

    fun toggleSelection(uri: String) {
        if (selected.contains(uri)) selected.remove(uri) else selected.add(uri)
        // Find the adapter position (+1 for camera offset) and refresh just that item
        val pos = uris.indexOf(uri)
        if (pos >= 0) notifyItemChanged(pos + 1)
    }

    fun clearSelection() {
        val old = selected.toSet()
        selected.clear()
        old.forEach { uri ->
            val pos = uris.indexOf(uri)
            if (pos >= 0) notifyItemChanged(pos + 1)
        }
    }

    fun getSelected(): Set<String> = selected.toSet()
}