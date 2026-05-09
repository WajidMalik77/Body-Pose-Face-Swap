package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemColorBinding
import androidx.core.graphics.toColorInt

class ColorAdapter(
    private val colors: List<String>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemColorBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.colorView.backgroundTintList =
            ColorStateList.valueOf(colors[position].toColorInt())

        holder.binding.colorView.setOnClickListener {
            onClick(colors[position].toColorInt())
        }
    }
}
