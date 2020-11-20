package com.novel.qingwen.utils

import android.text.Layout
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import java.lang.Math.round

/**
 * String分页测量工具
 * 根据字体 字号 文字内容 测量出每一行的文字数量，并计算给定高度@see setHeight()的页面内容分页
 * @Create by Frontman 2020/11/16
 */
object MeasurePage {
    private var width = 0
    private var height = 0
    private lateinit var mLayout:StaticLayout
    private val mTextPaint = TextPaint()
    private var text:String = ""

    fun getTextPaint() = mTextPaint

    fun setText(str: String){
        text = str
        mLayout = StaticLayout(
            text,
            mTextPaint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            1.0f,
            0.0f,
            false
        )
    }

    fun setHeight(height: Int){
        this.height = height
//        Log.e("height","$height")
    }

    fun setWidth(width:Int){
        this.width = width
//        Log.e("width","${MeasurePage.width}")
    }

    fun getPageString(str:String?=null):ArrayList<String>{
        if (str == null || str == "")return ArrayList()
        str?.let { setText(str) }
        if (width == 0 || height == 0)return arrayListOf(str.toString())
        val totalLineCount = mLayout.lineCount
//        Log.e("totalLineCount","$totalLineCount")
        var allowLineCount = height/ getLineHeight()
        if (allowLineCount < 1)allowLineCount = 1
        //通过以上参数分割字符串
        val pageLineCount = allowLineCount
//        Log.e("pageLineCount","$pageLineCount")
        var pageCount = totalLineCount/pageLineCount
        if (totalLineCount%pageLineCount > 0)
            pageCount++
        val list = ArrayList<String>()
//        var currentIndex = 0
        for (i in 0 until pageCount){
            var temp = (i+1)*pageLineCount
            temp--
            if (temp >= totalLineCount)
                temp = totalLineCount-1
            val start = mLayout.getLineStart(i*pageLineCount)
            val end = mLayout.getLineEnd(temp)
            val string = text.substring(start, end)
//            Log.e("MeasurePage","start$start end$end")
            list.add(string)
        }
//        for (i in 0 until  list.size) {
//            Log.e("MeasurePageView$i", list[i])
//        }
        return list
    }

    private fun getLineHeight():Int{
        return round(mTextPaint.getFontMetricsInt(null) * 1f + 0f)
    }
}