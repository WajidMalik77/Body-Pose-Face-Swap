package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R

class TemplateAdapter(
    private val templates: ArrayList<Int>,
    private val itemSize: Int,
    private val onTemplateClick: (resId: Int) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.VH>() {

    private val selectedResIds = mutableSetOf<Int>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val ivBadge: ImageView = view.findViewById(R.id.ivBadge)

        fun bind(resId: Int) {
            Glide.with(ivThumb)
                .load(resId)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(ivThumb)
            ivBadge.visibility = if (selectedResIds.contains(resId)) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onTemplateClick(resId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        view.layoutParams = RecyclerView.LayoutParams(itemSize, itemSize)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(templates[position])
    override fun getItemCount() = templates.size

    fun setSelected(resIds: Set<Int>) {
        selectedResIds.clear()
        selectedResIds.addAll(resIds)
        notifyDataSetChanged()
    }
}