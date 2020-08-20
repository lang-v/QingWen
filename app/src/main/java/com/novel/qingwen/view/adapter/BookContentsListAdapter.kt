package com.novel.qingwen.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.view.activity.ReadActivity
import com.novel.qingwen.viewmodel.ContentsVM

class BookContentsListAdapter(
    private val list: ArrayList<ContentsVM.ContentsInfo>,
    private val listener:ItemOnClickListener
) :
    RecyclerView.Adapter<BookContentsListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(
                    when (viewType) {
                        2 -> R.layout.simple_list_sub_item
                        else -> R.layout.simple_list_item
                    }, parent, false
                )
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        if (getItemViewType(position) == 2)
            holder.itemView.setOnClickListener {
//                ReadActivity.start(holder.itemView.context, novelId, item.id,novelName,status)
                listener.onClick(item)
            }
        else {

        }
    }

    override fun getItemViewType(position: Int): Int {
        //小于零为以及目录
        return if (list[position].id < 0L) 1
        else 2
    }


    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text)
    }
}
interface ItemOnClickListener{
    fun onClick(item:ContentsVM.ContentsInfo)
}