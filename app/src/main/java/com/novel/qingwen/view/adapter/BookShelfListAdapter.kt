package com.novel.qingwen.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.novel.qingwen.R
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.activity.ReadActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BookShelfListAdapter(
    private val values: ArrayList<BookInfo>
) : RecyclerView.Adapter<BookShelfListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_book_shelf_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        Glide.with(holder.img)
            .load(item.img)
            .into(holder.img)

        holder.title.text = item.novelName
        holder.newChapter.text = item.lastChapterName
        holder.newTime.text = item.lastUpdateTime

        holder.update.visibility = if(item.update) View.VISIBLE else View.GONE
        holder.delete.setOnClickListener{
            //移出
            GlobalScope.launch {
                RoomUtil.bookInfoDao.delete(item)
            }
            values.removeAt(position)
            notifyItemRemoved(position)
        }
        holder.item.setOnClickListener {
            if (item.lastReadId == -1L){
                Show.show(holder.itemView.context,"未知错误")
            }
            ReadActivity.start(holder.itemView.context,item.novelId,item.lastReadId,item.novelName,item.status,isInBookShelf = true)
            item.update = false
            //写入数据库
            GlobalScope.launch {
                RoomUtil.bookInfoDao.update(item)
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.img)
        val title: TextView = view.findViewById(R.id.title)
        val update:TextView = view.findViewById(R.id.update)
        val item :View = view.findViewById(R.id.bookShelfItem)
        val newTime :TextView = view.findViewById(R.id.newTime)
        val newChapter :TextView = view.findViewById(R.id.newChapter)
        val delete :TextView = view.findViewById(R.id.bookShelfListItemDelete)
    }
}