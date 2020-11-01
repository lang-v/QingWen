package com.novel.qingwen.view.adapter

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R

class BookStoreCategoryAdapter(
    private val resourceId:Int,
    val list: ArrayList<String>,
    private val block: (position: Int,item:Button) -> Unit
) : RecyclerView.Adapter<BookStoreCategoryAdapter.VH>() {
    private var selected = 0
    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(resourceId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        (holder.itemView as Button).text = list[position]
        holder.itemView.setOnClickListener {
            if(position != selected){
                val temp = selected
                selected = position
                notifyItemChanged(temp)
                holder.itemView.setTextColor(
                    Color.parseColor("#669900")
                )
            }
            block.invoke(position,holder.itemView)
        }
        holder.itemView.setTextColor(
            if (position == selected) Color.parseColor(
                "#669900"
            ) else
                ContextCompat.getColor(holder.itemView.context,R.color.textColorPrimary)
        )
        if(position == selected) {
            holder.itemView.performClick()
        }
    }

    override fun getItemCount(): Int = list.size
}
