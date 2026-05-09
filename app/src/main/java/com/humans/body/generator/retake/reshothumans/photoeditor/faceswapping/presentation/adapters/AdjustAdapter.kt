package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemAdjustBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PhotoEditListModel

class AdjustAdapter(
    private val imageList: ArrayList<PhotoEditListModel>,
    private val callbacks: (Int) -> Unit
) :
    RecyclerView.Adapter<AdjustAdapter.PoseViewHolder>() {

    inner class PoseViewHolder(val binding: ItemAdjustBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoseViewHolder {
        return PoseViewHolder(
            ItemAdjustBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PoseViewHolder, position: Int) {
        holder.binding.faceStyleImg.setImageResource(imageList[position].img)
        holder.binding.tv.text = (imageList[position].name)
        holder.itemView.setOnClickListener {
            callbacks.invoke(position)
        }
    }

    override fun getItemCount(): Int = imageList.size
}