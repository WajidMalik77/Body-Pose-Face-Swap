package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemSettingBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.Lang



class SettAdapter(
    private val courseList: ArrayList<Lang>,
    private val callBack: (Int) -> Unit
) : RecyclerView.Adapter<SettAdapter.CourseViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {

        return CourseViewHolder(
            ItemSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {

        holder.binding.imgLang.setImageResource(courseList[position].res)
        holder.binding.tvLang.text = (courseList[position].name)
        holder.binding.root.setOnClickListener {
            callBack.invoke( position)

        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemSettingBinding) :
        RecyclerView.ViewHolder(binding.root)
}
