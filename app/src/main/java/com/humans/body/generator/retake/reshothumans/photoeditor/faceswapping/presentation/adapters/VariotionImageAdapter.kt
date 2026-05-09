package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemVarImageBinding


class VariotionImageAdapter(
    private val courseList: List<Bitmap>,
    private val callBack: (Bitmap) -> Unit
) : RecyclerView.Adapter<VariotionImageAdapter.CourseViewHolder>() {

    private var lastCheckedPosition = 0

    fun setPos(pos: Int) {
        lastCheckedPosition = pos
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {

        return CourseViewHolder(
            ItemVarImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (lastCheckedPosition == position) {
            holder.binding.faceStyleImg.setBackgroundResource(R.drawable.image_variation_bg1)
        } else {
            holder.binding.faceStyleImg.setBackgroundResource(R.drawable.image_variation_bg)
        }
//        holder.binding.imgLang.setImageResource(courseList[position].res)
        holder.binding.faceStyleImg.setImageBitmap(courseList[position])
        holder.binding.root.setOnClickListener {
            callBack.invoke(courseList[position])


            val copyOfLastCheckedPosition: Int = lastCheckedPosition
            lastCheckedPosition = holder.adapterPosition
            notifyItemChanged(copyOfLastCheckedPosition)
            notifyItemChanged(lastCheckedPosition)
        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemVarImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}
