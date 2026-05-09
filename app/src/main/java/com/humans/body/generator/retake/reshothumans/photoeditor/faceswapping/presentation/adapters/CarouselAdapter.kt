package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemCarouselBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PhotoEditListModel

class CarouselAdapter(
    private val items: List<PhotoEditListModel> // images or layouts
) : RecyclerView.Adapter<CarouselAdapter.VH>() {

    inner class VH(val binding: ItemCarouselBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

        return VH(ItemCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.faceStyleImg.setImageResource(items[position].img)
        holder.binding.faceStyleTv.text = items[position].name
    }

    override fun getItemCount() = items.size
}
