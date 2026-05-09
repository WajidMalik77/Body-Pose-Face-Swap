package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.FragmentScreen1Binding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.LayoutFullscreenAdIntroBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.OnboardingItem
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.TYPE_AD

class OnboardingAdapter(
    private val items: List<OnboardingItem>,
    private val onBindIntroFullAd: ((LayoutFullscreenAdIntroBinding) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        Log.d("TAG", "onCreateViewHolder: $viewType")
        return when (viewType) {
            TYPE_AD -> {
                val binding = LayoutFullscreenAdIntroBinding.inflate(inflater, parent, false)
                AdViewHolder(binding)
            }
            else -> {
                val binding = FragmentScreen1Binding.inflate(inflater, parent, false)
                DataViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is DataViewHolder -> {
                holder.bind(item)
            }
            is AdViewHolder -> {
                onBindIntroFullAd?.invoke(holder.binding)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolders
    class DataViewHolder(val binding: FragmentScreen1Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnboardingItem) {
            binding.textView16.text = item.title
            item.imageRes?.let { binding.imageView3.setImageResource(it) }
        }
    }

    class AdViewHolder(val binding: LayoutFullscreenAdIntroBinding) :
        RecyclerView.ViewHolder(binding.root)
}
