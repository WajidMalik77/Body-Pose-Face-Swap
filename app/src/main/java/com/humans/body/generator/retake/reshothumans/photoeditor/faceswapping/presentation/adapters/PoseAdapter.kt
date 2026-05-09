package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemPoseImageBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemSettingBinding

class PoseAdapter (private val imageList: List<Int>) : RecyclerView.Adapter<PoseAdapter.PoseViewHolder>() {

    inner class PoseViewHolder(val binding: ItemPoseImageBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoseViewHolder {
        return PoseViewHolder(ItemPoseImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: PoseViewHolder, position: Int) {
        holder.binding.poseImage.setImageResource(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size
}