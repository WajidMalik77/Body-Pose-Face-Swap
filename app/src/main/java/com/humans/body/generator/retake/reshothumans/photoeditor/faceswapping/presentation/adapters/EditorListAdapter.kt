package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ItemEditorBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PhotoEditListModel


class EditorListAdapter(
    private val courseList: ArrayList<PhotoEditListModel>,
    private val callBack: (Int, String) -> Unit
) : RecyclerView.Adapter<EditorListAdapter.CourseViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {

        return CourseViewHolder(
            ItemEditorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {

        holder.binding.faceStyleImg.setImageResource(courseList[position].img)
        holder.binding.faceStyleTv.text = (courseList[position].name)
        if (courseList[position].isFree) holder.binding.free.visibility = View.VISIBLE
        else holder.binding.free.visibility = View.GONE
        holder.binding.root.setOnClickListener {
            callBack.invoke( position,courseList[position].name)

        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemEditorBinding) :
        RecyclerView.ViewHolder(binding.root)
}
