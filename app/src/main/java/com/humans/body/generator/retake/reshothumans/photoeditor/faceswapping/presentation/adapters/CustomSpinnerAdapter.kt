package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R

class CustomSpinnerAdapter(
    private val context: Context,
    private val items: List<String>
) : BaseAdapter()
{

    private var selectedPosition = -1

    fun setSelected(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun isEnabled(position: Int): Boolean {
        // Disable "Select Gender"
        return position != 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.itemText)
        val icon = view.findViewById<ImageView>(R.id.itemIcon)

        if (position == 0) {
            textView.setTextColor(Color.GRAY) // hint in dropdown
            icon.visibility = View.GONE
        } else {
//            textView.setTextColor(Color.BLACK)
            icon.visibility = View.VISIBLE


        }

        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)
        val textView = view.findViewById<TextView>(R.id.itemText)
        val icon = view.findViewById<ImageView>(R.id.itemIcon)

        textView.text = items[position]


        // Show selected toggle icon (skip for hint item)
        if (position == selectedPosition && position != 0) {
            icon.setImageResource(R.drawable.tick_pink)
        } else {
            icon.setImageResource(R.drawable.unselect_tick)
        }
        if (position==0)
            icon.setImageResource(R.drawable.drop_down_icon)

        return view
    }
}

