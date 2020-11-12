package com.novel.qingwen.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.novel.qingwen.R
import sl.view.elasticviewlibrary.ElasticLayout

class BookStoreRefreshHeader(private val context: Context, offset: Int) : ElasticLayout.HeaderAdapter(offset) {
    private lateinit var view: View
    private val icon by lazy { view.findViewById<ImageView>(R.id.novelImg) }
    private val progressBar by lazy { view.findViewById<ProgressBar>(R.id.progressBar) }
    private val text by lazy { view.findViewById<TextView>(R.id.text) }

    //icon的方向
    private val DIRECTION_DOWN = true
    private val DIRECTION_UP = false
    private var direction = DIRECTION_DOWN

    override fun getContentView(viewGroup: ViewGroup): View {
        view = LayoutInflater.from(context).inflate(R.layout.base_layout, viewGroup, false)
        text.visibility = View.GONE
        return view
    }
    override fun scrollProgress(progress: Int) {}


    override fun pullToDo() {
        if (direction == DIRECTION_UP) {
            direction = DIRECTION_DOWN
        }
        icon.rotation = 0f
        super.pullToDo()
    }

    override fun releaseToDo() {
        if (direction == DIRECTION_DOWN) {
            icon.rotation = 180F
            direction = DIRECTION_UP
        }
        super.releaseToDo()
    }

    override fun onDo() {
        progressBar.visibility = View.VISIBLE
        icon.rotation = 0f
        icon.visibility = View.INVISIBLE
        super.onDo()
    }
    override fun onDone(msg:String) {
        progressBar.visibility = View.INVISIBLE
        icon.visibility = View.VISIBLE
        direction = DIRECTION_DOWN
        super.onDone(msg)
    }
}