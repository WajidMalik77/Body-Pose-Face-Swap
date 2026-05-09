package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemVarTextBinding


class VariotionTextAdapter(
    private val courseList: ArrayList<Int>,
    private val callBack: ( Int) -> Unit
) : RecyclerView.Adapter<VariotionTextAdapter.CourseViewHolder>() {

    private var lastCheckedPosition = 0

    fun setPos(pos: Int) {
        lastCheckedPosition = pos
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {

        return CourseViewHolder(
            ItemVarTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (lastCheckedPosition == position) {
            holder.binding.faceStyleTv.setBackgroundResource(R.drawable.image_variation_bg1)
        } else {
            holder.binding.faceStyleTv.setBackgroundResource(R.drawable.image_variation_bg)
        }
//        holder.binding.imgLang.setImageResource(courseList[position].res)
        holder.binding.faceStyleTv.text = (courseList[position].toString())
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

    class CourseViewHolder(val binding: ItemVarTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}
