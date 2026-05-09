package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Typeface
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemFontBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.FontItem
import kotlin.collections.joinToString
import kotlin.text.isLowerCase
import kotlin.text.replace
import kotlin.text.replaceFirstChar
import kotlin.text.split
import kotlin.text.titlecase

class FontAdapter(
    private val fonts: List<FontItem>,
    private val onItemClick: (FontItem, String) -> Unit
) : RecyclerView.Adapter<FontAdapter.FontViewHolder>() {

    inner class FontViewHolder(val binding: ItemFontBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        return FontViewHolder(ItemFontBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
        val fontItem = fonts[position]


        val displayName = fontItem.assetFileName
            .replace(Regex("(?i)\\.ttf|\\.otf"), "") // Remove .ttf or .otf (case-insensitive)
            .split("_", " ", "-") // Split on underscore, space, or dash
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }

        holder.binding.faceStyleTv.text = displayName
//        holder.fontFileNameText.text = displayName

        val typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/${fontItem.assetFileName}")


//        holder.nameText.text = "Font Library"
        holder.binding.faceStyleTv.typeface = typeface
//        holder.fontFileNameText.typeface = typeface

        holder.itemView.setOnClickListener {
            onItemClick(fontItem, displayName)
        }

        Log.d("FontAdapter", "Item clicked: $displayName")

    }

    override fun getItemCount(): Int = fonts.size
}