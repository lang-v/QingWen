package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.utils.ConfigUtil

class CustomTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    //    constructor(context: Context) : this(context,null)
//    constructor(context: Context,attributeSet: AttributeSet?):this(context,attributeSet,0)
//    constructor(context: Context,attributeSet: AttributeSet?,defStyleAtt:Int):super(context,attributeSet,defStyleAtt){
    init {
        val config = ConfigUtil.getConfig()
        setTextColor(config.textColor)
        textSize = config.textSize.toFloat()
        //-1 为使用系统默认字体
        if (config.textStyle != -1) {
            typeface = ResourcesCompat.getFont(
                context,
                if (config.textStyle == 0) R.font.ht else R.font.kt
            )
        }
    }

}
