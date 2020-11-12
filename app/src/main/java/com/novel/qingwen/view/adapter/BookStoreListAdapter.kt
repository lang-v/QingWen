package com.novel.qingwen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.novel.qingwen.R
import com.novel.qingwen.net.bean.Book

class BookStoreListAdapter(private val list: ArrayList<Book>,private val block:(item:Book,view:View)->Unit) :
    RecyclerView.Adapter<BookStoreListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val img: ImageView = view.findViewById(R.id.novelImg)
        val author: TextView = view.findViewById(R.id.author)
        val resume: TextView = view.findViewById(R.id.resume)
        val tags: LinearLayout = view.findViewById(R.id.tags)
        val vote:TextView = view.findViewById(R.id.booksStoreVote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_book_store_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        Glide.with(holder.img).load("https://imgapixs.pysmei.com/BookFiles/BookImages/"+item.Img)
            .error(R.drawable.notfoundpic)
            .into(holder.img)
        holder.title.text = item.Name
        holder.resume.text = item.Desc
        holder.author.text = item.Author
        val view: TextView =
            LayoutInflater.from(holder.tags.context)
                .inflate(R.layout.tag_item, holder.tags, false) as TextView
        view.text = item.CName
        holder.tags.removeAllViews()
        holder.tags.addView(view)
        holder.vote.text = list[position].Score.toString()

        holder.itemView.setOnClickListener {
            block.invoke(item,holder.img)
//            ResumeActivity.start(holder.itemView.context, item.Id.toLong(), item.Name)
        }
    }
}