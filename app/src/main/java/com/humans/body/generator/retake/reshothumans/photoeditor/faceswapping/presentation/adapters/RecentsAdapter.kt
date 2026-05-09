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

class RecentsAdapter(
    rawItems: List<String>,
    private val itemSize: Int,
    private val onRecentClick: (value: String) -> Unit
) : RecyclerView.Adapter<RecentsAdapter.VH>() {

    private val items = rawItems.filter { it.isNotBlank() }
    private var selectedValue: String? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val ivCamera: ImageView = view.findViewById(R.id.ivCamera)
        val ivBadge: ImageView = view.findViewById(R.id.ivBadge)

        fun bind(value: String) {
            ivCamera.visibility = View.GONE
            ivBadge.visibility = if (value == selectedValue) View.VISIBLE else View.GONE

            val resId = value.toIntOrNull()
            if (resId != null) {
                Glide.with(ivThumb)
                    .load(resId)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(ivThumb)
            } else {
                Glide.with(ivThumb)
                    .load(value.toUri())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(ivThumb)
            }
            itemView.setOnClickListener { onRecentClick(value) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        view.layoutParams = RecyclerView.LayoutParams(itemSize, itemSize)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    fun setSelected(value: String?) {
        val old = selectedValue
        selectedValue = value
        old?.let { oldValue ->
            val oldIndex = items.indexOf(oldValue)
            if (oldIndex >= 0) notifyItemChanged(oldIndex)
        }
        value?.let { newValue ->
            val newIndex = items.indexOf(newValue)
            if (newIndex >= 0) notifyItemChanged(newIndex)
        }
    }
}
