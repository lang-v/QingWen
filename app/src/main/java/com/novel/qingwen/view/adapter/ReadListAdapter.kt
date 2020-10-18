package com.novel.qingwen.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.room.entity.Chapter
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.view.widget.CustomTextView

class ReadListAdapter(
    private var list: ArrayList<Chapter>
) : RecyclerView.Adapter<ReadListAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: CustomTextView = view.findViewById(R.id.readItemText)
        val title: CustomTextView = view.findViewById(R.id.readItemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.activity_read_item, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
//        holder.text.setText(list[position].content,TextView.BufferType.SPANNABLE)
        holder.text.text = list[position].content
        holder.title.text = list[position].name
        holder.title.textSize = ConfigUtil.getTextSize().toFloat() + 5
        //不拦截所有事件
//        holder.itemView.setOnTouchListener { _, _ -> false}
        //点击事件给activity处理
//        holder.itemView.setOnTouchListener { v, event ->
//            Log.e("item","onTouch")
//            false
//        }
//        holder.text.setOnClickListener{
////            onClickListener.onClick(it)
//            Log.e("item","onClick")
//        }
    }
}