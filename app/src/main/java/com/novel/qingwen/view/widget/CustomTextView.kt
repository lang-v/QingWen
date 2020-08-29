package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.novel.qingwen.utils.ConfigUtil

class CustomTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    //    constructor(context: Context) : this(context,null)
//    constructor(context: Context,attributeSet: AttributeSet?):this(context,attributeSet,0)
//    constructor(context: Context,attributeSet: AttributeSet?,defStyleAtt:Int):super(context,attributeSet,defStyleAtt){
    init {
        setTextColor(ConfigUtil.getTextColor())
        textSize = ConfigUtil.getTextSize().toFloat()
        //0 为系统默认字体无须设置
        if (ConfigUtil.getTextStyle() != 0)
            typeface = ResourcesCompat.getFont(context, ConfigUtil.getTextStyle())
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
    }
}
