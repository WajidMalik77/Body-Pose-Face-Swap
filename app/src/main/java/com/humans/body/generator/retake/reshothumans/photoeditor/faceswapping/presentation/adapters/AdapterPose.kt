package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemPoseBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.isPremium

class AdapterPose(private val items: ArrayList<Int>,
    private val onItemSelected: (ImageView) -> Unit) : RecyclerView.Adapter<AdapterPose.OptionViewHolder>() {

    private var selectedPosition = -1
    fun updateData(newItems: ArrayList<Int>) {
        items.clear() // now safe
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class OptionViewHolder(val binding: ItemPoseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int, position: Int) {
            val premium = isPremium(binding.root.context)

            val isUnlocked = premium || position == 0

            // Update selection state
            if (position == selectedPosition) {
                binding.img1.visibility = View.VISIBLE
            } else {
                binding.img1.visibility = View.GONE
            }
            binding.free.visibility = if (isUnlocked) View.GONE else View.VISIBLE

            binding.img.setImageResource(item)
            binding.root.isClickable = isUnlocked

            // Handle click
            binding.root.setOnClickListener {
            /*    val oldPos = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                onItemSelected(binding.img)*/
                if (isUnlocked) {
                    binding.root.setOnClickListener {
                        val oldPos = selectedPosition
                        selectedPosition = position
                        notifyItemChanged(oldPos)
                        notifyItemChanged(selectedPosition)
                        onItemSelected(binding.img)
                    }
                } else {
                    Toast.makeText(binding.root.context,
                        binding.root.context.getString(R.string.this_item_locked), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemPoseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
