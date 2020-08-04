package com.novel.qingwen.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.novel.qingwen.R
import com.novel.qingwen.bean.BookInfo


class BookShelfListAdapter(
    private val values: List<BookInfo>
) : RecyclerView.Adapter<BookShelfListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_book_shelf_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: TextView = view.findViewById(R.id.img)
        val title: TextView = view.findViewById(R.id.title)
        val newChapter :TextView = view.findViewById(R.id.newChapter)
        val newTime :TextView = view.findViewById(R.id.newTime)
        val fresh:TextView = view.findViewById(R.id.fresh)
    }
}