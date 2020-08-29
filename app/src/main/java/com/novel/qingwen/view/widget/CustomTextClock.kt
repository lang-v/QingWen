package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextClock
import com.novel.qingwen.utils.ConfigUtil

class CustomTextClock @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextClock(context, attrs, defStyleAttr) {
    init {
        setTextColor(ConfigUtil.getTextColor())
    }
}