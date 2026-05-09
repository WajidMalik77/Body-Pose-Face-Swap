package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemAdjustBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.filters.FilterType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Filters

class FilterAdapter(
    private val filters: ArrayList<Filters>,
    private val onClick: (FilterType) -> Unit
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdjustBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(type: Filters) {
            binding.tv.text = type.name
            binding.faceStyleImg.setImageResource(type.img)

            binding.root.setOnClickListener { onClick(type.type)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemAdjustBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount() = filters.size
}
