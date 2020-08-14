package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerView: RecyclerView{
    constructor(context: Context) : super(context)
    constructor(context: Context,attributeSet: AttributeSet?):super(context,attributeSet)
    constructor(context: Context,attributeSet: AttributeSet?,defStyleAtt:Int):super(context,attributeSet,defStyleAtt)
    //拦截所有
//    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
//        if (e?.action == MotionEvent.ACTION_DOWN)
//            return true
//        return false
//    }
}
