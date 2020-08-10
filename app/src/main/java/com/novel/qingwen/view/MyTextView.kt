package com.novel.qingwen.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyTextView: androidx.appcompat.widget.AppCompatTextView{
    constructor(context: Context) : super(context)
    constructor(context: Context,attributeSet: AttributeSet?):super(context,attributeSet)
    constructor(context: Context,attributeSet: AttributeSet?,defStyleAtt:Int):super(context,attributeSet,defStyleAtt)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }
}