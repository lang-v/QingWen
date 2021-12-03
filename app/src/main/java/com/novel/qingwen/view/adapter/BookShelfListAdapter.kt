package com.novel.qingwen.view.adapter

import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.novel.qingwen.R
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.PopupWindowUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.activity.ReadActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class BookShelfListAdapter(
    private val values: ArrayList<BookInfo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val NonBook = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                if (viewType == NonBook) R.layout.fragment_book_shelf_list_item_nobook else R.layout.fragment_book_shelf_list_item,
                parent,
                false
            )
        return if (viewType == NonBook) NonBookViewHolder(view) else ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val item = values[position]
            Glide.with(holder.img)
                .load(item.img)
                .placeholder(R.drawable.notfoundpic)
                .error(R.drawable.notfoundpic)
                .into(holder.img)

            holder.title.text = item.novelName
            holder.newChapter.text = item.lastChapterName
            holder.newTime.text = item.lastUpdateTime

            holder.update.visibility = if (item.update) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener {
                if (item.lastReadId == -1L) {
                    Show.show(holder.itemView.context, "未知错误")
                }
                BookShelfListUtil.currentBookInfo = item
                ReadActivity.start(
                    holder.itemView.context,
                    item.novelId,
                    item.lastReadId,
                    item.lastReadOffset,
                    item.novelName,
                    item.status,
                    isInBookShelf = true
                )
                item.update = false
                //写入数据库
                GlobalScope.launch {
                    RoomUtil.bookInfoDao.update(item)
                    GlobalScope.launch (Dispatchers.Main){
                        notifyItemChanged(position)
                    }
                }
            }
            holder.itemView.setOnLongClickListener {
                PopupWindowUtil.showPopupWindow(holder.itemView) {
//                    移出
                    BookShelfListUtil.remove(item)
                    refresh()
                }
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (values[position].novelId == -1L) NonBook
        else 0
    }


    fun refresh() {
        if (itemCount > 1 && values[0].novelId == -1L) {
            values.removeAt(0)
            notifyItemRemoved(0)
            notifyItemRangeInserted(1,itemCount)
            return
        }
        if (itemCount == 0) {
            values.add(
                BookInfo(
                    -1, "", ",", false,
                    -1L, 0, "", -1L,
                    -1L, "", ""
                )
            )
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.novelImg)
        val title: TextView = view.findViewById(R.id.title)
        val update: TextView = view.findViewById(R.id.update)

        //        val item: View = view.findViewById(R.id.bookShelfItem)
        val newTime: TextView = view.findViewById(R.id.newTime)
        val newChapter: TextView = view.findViewById(R.id.newChapter)
//        val delete: TextView = view.findViewById(R.id.bookShelfListItemDelete)
    }

    class NonBookViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemId(position: Int): Long {
        val name = values[position].novelName
        return if (TextUtils.isEmpty(name)) {
            // 记录一下小问题，super.getItemId() 始终返回-1
            // super.getItemId(position)
            values[position].hashCode().toLong()
        }else {
            name.hashCode().toLong()
        }
    }

}