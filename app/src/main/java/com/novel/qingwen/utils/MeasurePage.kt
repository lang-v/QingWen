package com.novel.qingwen.utils

import android.text.*
import android.util.Log
import android.widget.TextView
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
    private lateinit var mLayout: Layout
    private var mTextPaint = TextPaint()
    private var text: String = ""
    private var viewReference: WeakReference<ReadView>? = null
//    private var lineSpaceExtra = 0f
//    private var lineSpaceMult = 1f
//    private var lineHeight = 0f

    private fun attachView(view: ReadView) {
        viewReference = WeakReference(view)
    }

    private fun detachView() {
        viewReference = null
    }

    private fun setText(str: String) {
        while (initLock) Thread.yield()
        synchronized(this) {
            text = str
            mLayout = StaticLayout(
                text,
                viewReference?.get()?.textPaint ?: mTextPaint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                viewReference?.get()?.lineSpaceMult ?: 1.0f,
                viewReference?.get()?.lineSpaceExtra ?: 0f,
                false
            )
        }
    }

    private fun setHeight(height: Int) {
        this.height = height
    }

    private fun setWidth(width: Int) {
        this.width = width
    }

    var initLock = false
    fun initView(view: ReadView) {
        if (initLock) return
        initLock = true
        view.post {
            setWidth(view.width - view.paddingStart - view.paddingEnd)
//            Log.e("view.width","${width}")
            setHeight(
                view.height
                        - view.paddingTop
                        - view.paddingBottom
            )
            mTextPaint = view.textPaint
            viewReference = WeakReference(view)
            initLock = false
        }
    }


    fun test() {

        val lineSpaceadd = 0.0f
        val lineSpacemuti = 1.0f
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mLayout = StaticLayout.Builder
                .obtain("", 0, 0, TextPaint(), width)
                .build()
        } else {
            mLayout = StaticLayout(
                "传入的String",
                TextPaint(),
                width,
                Layout.Alignment.ALIGN_NORMAL,
                lineSpaceadd,
                lineSpacemuti,
                false
            )
        }

        mLayout.lineCount//获取行数
//        mLayout.draw()
        mLayout.getLineStart(0)//获取第一行在传入String中的起始位置
        mLayout.getLineEnd(0)//获取第一行在传入String中的终止位置
        mLayout.getLineVisibleEnd(2)//获取指定行的最后可见字符（不计算空格的文本偏移量）
    }

    fun getPageString(str: String? = null): ArrayList<String> {
        while (initLock) Thread.yield()
        synchronized(this) {
            if (width == 0 || height == 0) return arrayListOf(str.toString())
            str?.let { setText(it) }
            val totalLineCount = mLayout.lineCount
            var allowLineCount =
                (height).toInt() / getLineHeight()
            if (allowLineCount < 1) allowLineCount = 1
            //通过以上参数分割字符串
            val pageLineCount = allowLineCount
            var pageCount = totalLineCount / pageLineCount
            if (totalLineCount % pageLineCount > 0)
                pageCount++
            val list = ArrayList<String>()
            for (i in 0 until pageCount) {
                var temp = (i + 1) * pageLineCount
                temp--
                if (temp >= totalLineCount)
                    temp = totalLineCount - 1
                val start = mLayout.getLineStart(i * pageLineCount)
                val end = mLayout.getLineEnd(temp)
                val string = text.substring(start, end)
                list.add(string)
            }
            return list
        }
    }

    private fun getLineHeight(): Int {
        return viewReference?.get()?.getLineHeight()?.toInt() ?: round(
            mTextPaint.getFontMetricsInt(
                null
            ) * 1f + 0f
        )
    }
}