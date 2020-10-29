package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class CustomInterruptTouchEventLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var interrupt = false

    fun setInterrupt(value:Boolean){
        interrupt = value
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (interrupt){

        }
        return super.onInterceptTouchEvent(ev)
    }

}