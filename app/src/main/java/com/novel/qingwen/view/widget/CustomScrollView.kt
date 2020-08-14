package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class CustomScrollView: ScrollView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?):super(context,attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAtt:Int):super(context,attributeSet,defStyleAtt)

    var onScrollListener: OnScrollListener? = null
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (onScrollListener == null) return
        if (!canScrollVertically(1)){
            onScrollListener!!.bottom()
        }
        else if(!canScrollVertically(-1)){
            onScrollListener!!.top()
        }
    }

    //到达底部和顶部时将调用
    interface OnScrollListener{
        fun top()
        fun bottom()
    }
}