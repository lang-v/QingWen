package com.novel.qingwen.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.room.entity.Chapter

class ReadListAdapter(private var list:ArrayList<Chapter>): RecyclerView.Adapter<ReadListAdapter.VH>() {
    class VH(view: View):RecyclerView.ViewHolder(view){
        val text: TextView = view.findViewById(R.id.readItemText)
        val title:TextView = view.findViewById(R.id.readItemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.read_item,parent,false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = list[position].content
        holder.title.text = list[position].name
    }

    fun setData(list:ArrayList<Chapter>){
        this.list = list
        notifyItemChanged(0)
//        notifyItemChanged(list.size-1)
//        notifyDataSetChanged()
    }
}