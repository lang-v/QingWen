package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class MarqueeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
    override fun isFocused(): Boolean {
        return true
    }
}