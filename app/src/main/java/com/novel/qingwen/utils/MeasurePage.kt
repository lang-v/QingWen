package com.novel.qingwen.utils

import android.text.*
import android.util.Log
import com.novel.qingwen.view.widget.ReadView
import java.lang.Exception
import java.lang.Math.round
import java.lang.ref.WeakReference

/**
 * String分页测量工具
 * 根据字体 字号 文字内容 测量出每一行的文字数量，并计算给定高度@see setHeight()的页面内容分页
 * @Create by Frontman 2020/11/16
 */
object MeasurePage {
    private var width = 0
    private var height = 0
    private lateinit var mLayout:Layout
    private var mTextPaint = TextPaint()
    private var text:String = ""
    private var viewReference : WeakReference<ReadView>? = null

    private var textLock = false
    fun setText(str: String){
        while (textLock)Thread.yield()
        text = str
        mLayout = StaticLayout(
            text,
            viewReference?.get()?.textPaint?: mTextPaint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            viewReference?.get()?.lineSpaceMult?:1.0f,
            viewReference?.get()?.lineSpaceExtra?:0.0f,
            false
        )
    }

    fun setHeight(height: Int){
        this.height = height
    }

    fun setWidth(width:Int){
        this.width = width
    }

    var initLock=false
    fun initView(view: ReadView){
        if (initLock)return
        initLock=true
        view.post {
            setWidth(view.width-view.paddingStart-view.paddingEnd)
//            Log.e("view.width","${width}")
            setHeight(
                view.height
                        -view.paddingTop
                        -view.paddingBottom
            )
            mTextPaint = view.textPaint
            viewReference = WeakReference(view)
//            Log.e("view.height","${height}")
//            mTextPaint.textSize=view.textSize
//            mLayout=view.layout
//            mLayout.topPadding=view.totalPaddingTop
//            mLayout.getLineVisibleEnd()
            initLock=false
        }
    }

    fun getPageString(str:String?=null):ArrayList<String>{
        while (initLock || textLock)Thread.yield()
        if (str == null || str == "")return ArrayList()
        str?.let { setText(str) }
        if (!textLock) textLock=true
        if (width == 0 || height == 0)return arrayListOf(str.toString())
        val totalLineCount = mLayout.lineCount
//        Log.e("totalLineCount","$totalLineCount")
        var allowLineCount = (height)/ getLineHeight()
        if (allowLineCount < 1)allowLineCount = 1
        //通过以上参数分割字符串
        val pageLineCount = allowLineCount
//        Log.e("pageLineCount","$pageLineCount")
        var pageCount = totalLineCount/pageLineCount
        if (totalLineCount%pageLineCount > 0)
            pageCount++
        val list = ArrayList<String>()
//        var currentIndex = 0
            for (i in 0 until pageCount) {
                var temp = (i + 1) * pageLineCount
                temp--
                if (temp >= totalLineCount)
                    temp = totalLineCount - 1
                val start = mLayout.getLineStart(i * pageLineCount)
                val end = mLayout.getLineEnd(temp)
                try {
                    val string = text.substring(start, end)
//            Log.e("MeasurePage","start$start end$end")
                    list.add(string)
                }catch (e:Exception){
                    //todo release包在vivo机上偶尔会出现指针溢出，可能是代码混淆
                    e.printStackTrace()
                }
            }
//        for (i in 0 until  list.size) {
//            Log.e("MeasurePageView$i", list[i])
//        }
        textLock=false
        return list
    }

    private fun getLineHeight():Int{
        return viewReference?.get()?.getLineHeight()?.toInt()?:round(mTextPaint.getFontMetricsInt(null) * 1f + 0f)
    }
}