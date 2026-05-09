package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemFirstBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemFirstTwoBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemFourBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemImageBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemTitleBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.MixedItem
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_FIRST
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_FIRST_TWO
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_FOUR_VERTICAL
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_LAST_TWO
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_TITLE

class MixedAdapter(private val callback: (Int, Bitmap?) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<MixedItem>? = null
    fun updateData(newItems: List<MixedItem>) {
        items = newItems
        notifyDataSetChanged() // later you can upgrade to DiffUtil
    }

    override fun getItemViewType(position: Int): Int = items!![position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FIRST -> {
                val binding = ItemFirstBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FirstVH(binding)
            }

            TYPE_FIRST_TWO -> {
                val binding = ItemFirstTwoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FirstTwoVH(binding)
            }

            TYPE_FOUR_VERTICAL -> {
                val binding = ItemFourBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FourVerticalVH(binding)
            }

            TYPE_TITLE -> {
                val binding = ItemTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TitleVH(binding)
            }

            TYPE_LAST_TWO -> {
                val binding = ItemImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LastTwoVH(binding)
            }

            else -> {
                val binding = ItemTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TitleVH(binding)
            }
        }
    }

    override fun getItemCount() = items?.size!!

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items!![position]
        holder.itemView.setOnClickListener {
            callback.invoke(
                position,
                if (holder is LastTwoVH) holder.binding.faceStyleImg.drawable.toBitmap() else null
            )
        }
        when (holder) {
            is FirstVH -> {

            }

            is FirstTwoVH -> {
                holder.binding.selectPosImg.setImageResource(item.imageRes)
                holder.binding.selectPos.setBackgroundResource(item.bg)
                holder.binding.selectPosTv.text = item.title
                holder.binding.selectPosTv.isSelected = true

            }

            is FourVerticalVH -> {
                holder.binding.faceStyleImg.setImageResource(item.imageRes)
                holder.binding.faceStyle.setBackgroundResource(item.bg)
                holder.binding.faceStyleTv.text = item.title
                holder.binding.faceStyleTv.isSelected = true
                if (item.isFree) holder.binding.free.visibility = View.VISIBLE
                else holder.binding.free.visibility = View.GONE


            }

            is TitleVH -> holder.binding.faceStyleTv.text = item.title

            is LastTwoVH -> {
                holder.binding.faceStyleImg.setImageResource(item.imageRes)
            }
        }
    }

    class FirstVH(val binding: ItemFirstBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FirstTwoVH(val binding: ItemFirstTwoBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FourVerticalVH(val binding: ItemFourBinding) :
        RecyclerView.ViewHolder(binding.root)

    class TitleVH(val binding: ItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root)

    class LastTwoVH(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)
}
