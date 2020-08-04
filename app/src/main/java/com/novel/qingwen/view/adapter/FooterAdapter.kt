package com.novel.qingwen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.elasticviewlibrary.BaseHeader
import com.example.elasticviewlibrary.ElasticView
import com.novel.qingwen.R

class FooterAdapter(offset:Int): ElasticView.FooterAdapter(offset) {
    lateinit var view:TextView
    override fun getFooterView(viewGroup: ViewGroup): View {
        view = LayoutInflater.from(viewGroup.context).inflate(R.layout.footer_layout,viewGroup,false) as TextView
        return view
    }

    override fun onLoad() {
    }

    override fun pullToLoad() {

    }

    override fun releaseToLoad() {

    }

    override fun scrollProgress(progress: Int) {

    }

    override fun setPullCallBack(callBack: ElasticView.PullCallBack) {

    }

}