package com.novel.qingwen.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.viewmodel.ContentsVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookContentsListAdapter(
    private val list: ArrayList<ContentsVM.ContentsInfo>,
    private val listener: ItemOnClickListener
) :
    RecyclerView.Adapter<BookContentsListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(
                    when (viewType) {
                        2 -> R.layout.activity_contents_list_sub_item
                        else -> R.layout.activity_contents_list_item
                    }, parent, false
                )
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        if (getItemViewType(position) == 2) {
            holder.itemView.setOnClickListener {
//                ReadActivity.start(holder.itemView.context, novelId, item.id,novelName,status)
                listener.onClick(item)
            }
            if (item.isSelect) {
                holder.itemView.setBackgroundColor(ConfigUtil.getBackgroundColor())
                holder.name.setTextColor(ConfigUtil.getTextColor())
            }else{
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        //小于零为一级目录
        return if (list[position].id < 0L) 1
        else 2
    }

    private var lastSelected: Int = -1

    //选中某个item
    fun selected(id: Long): Int {
        try {
            for (i in 0 until list.size) {
                if (list[i].id == id) {
                    if (lastSelected >= 0)
                        list[lastSelected].isSelect = false
                    list[i].isSelect = true
                    GlobalScope.launch(Dispatchers.Main) {
                        notifyItemChanged(lastSelected)
                        notifyItemChanged(i)
                        lastSelected = i
                    }
                    return i
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text)
    }
}

interface ItemOnClickListener {
    fun onClick(item: ContentsVM.ContentsInfo)
}