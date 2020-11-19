package com.novel.qingwen.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.view.widget.CustomTextView

class ReadListAdapter(
    private var list: ArrayList<Chapter>
) : RecyclerView.Adapter<ReadListAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: CustomTextView = view.findViewById(R.id.readItemText)
//        val title: CustomTextView = view.findViewById(R.id.readItemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.activity_read_item, parent, false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = list[position].content
//        holder.title.text = list[position].name
//        holder.title.textSize = ConfigUtil.getTextSize().toFloat() + 5
    }

    data class Chapter(
        //章节id
        override val novelId: Long,
        //当前小说id
        override val chapterId: Long,
        //章节名
        override val name: String,
        //章节内容
        override val content: String,
        //下一、章
        override val nid: Long,
        //上一章
        override val pid: Long,
        //标志当前item是  输入  头部:-1 中间内容:0 尾部:1
        var type: Int = 0,
        //记录当前item 位于当前章节的位置
        var index: Int = 0,
        var totalPage:Int =0
    ) : com.novel.qingwen.room.entity.Chapter(novelId, chapterId, name, content, nid, pid)
}