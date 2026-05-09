package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemLangBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Lang



class LangAdapter(
    private val courseList: ArrayList<Lang>,
    private val callBack: (String, Int) -> Unit
) : RecyclerView.Adapter<LangAdapter.CourseViewHolder>() {

    private var lastCheckedPosition = 0

    fun setPos(pos: Int) {
        lastCheckedPosition = pos
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {

        return CourseViewHolder(
            ItemLangBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (lastCheckedPosition == position) {
            holder.binding.img.setImageResource(R.drawable.lang_selected)
        } else {
            holder.binding.img.setImageResource(R.drawable.lang_unselected)
        }
        holder.binding.imgLang.setImageResource(courseList[position].res)
        holder.binding.tvLang.text = (courseList[position].name)
        holder.binding.root.setOnClickListener {
            callBack.invoke(courseList[position].name, position)


            val copyOfLastCheckedPosition: Int = lastCheckedPosition
            lastCheckedPosition = holder.adapterPosition
            notifyItemChanged(copyOfLastCheckedPosition)
            notifyItemChanged(lastCheckedPosition)
        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemLangBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}
