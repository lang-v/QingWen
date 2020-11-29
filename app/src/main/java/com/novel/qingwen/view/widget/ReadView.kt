package com.novel.qingwen.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.novel.qingwen.utils.ConfigUtil

/**
 * 上下分别间隔 1/2 lineSpaceExtra
 */
class ReadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var lineSpaceMult = 1.0f
    var lineSpaceExtra = 20f
    var text: String = ""
        set(value) {
            field = value
            post {
                mLayout = StaticLayout(
                    value,
                    textPaint,
                    width,
                    Layout.Alignment.ALIGN_NORMAL,
                    lineSpaceMult,
                    lineSpaceExtra,
                    false
                )
                invalidate()
            }
        }
    var textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = ConfigUtil.getTextColor()
        textSize = ConfigUtil.getTextSize()
        this.isAntiAlias = true
        if (ConfigUtil.getTextStyle() != 0)
            ResourcesCompat.getFont(context, ConfigUtil.getTextStyle())?.let { setTypeface(it) }
    }

    var mLayout =
        StaticLayout(
            text,
            textPaint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            lineSpaceMult,
            lineSpaceExtra,
            false
        )


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var wantWidth = textPaint.measureText(text).toInt()
        if (wantWidth < minimumWidth)
            wantWidth = minimumWidth
        when (widthMode) {
            MeasureSpec.AT_MOST -> {
                if (wantWidth > widthSize)
                    wantWidth = widthSize
            }
            MeasureSpec.EXACTLY -> {
                wantWidth = widthSize
            }
            MeasureSpec.UNSPECIFIED -> {
            }
            else -> {
                wantWidth = 0
            }
        }

        var wantHeight = StaticLayout(
            text,
            textPaint,
            wantWidth,
            Layout.Alignment.ALIGN_NORMAL,
            lineSpaceMult,
            lineSpaceExtra,
            false
        ).lineCount * getLineHeight().toInt() + lineSpaceExtra.toInt()
        if (wantHeight < minimumHeight)
            wantHeight = minimumHeight
        when (heightMode) {
            MeasureSpec.AT_MOST -> {
                if (wantHeight > heightSize)
                    wantHeight = heightSize
            }
            MeasureSpec.EXACTLY -> {
                wantHeight = heightSize
            }
            MeasureSpec.UNSPECIFIED -> {
            }
            else -> {
                wantHeight = 0
            }
        }
        setMeasuredDimension(wantWidth, wantHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        val count = getLineCount()
        for (i in 0 until count) {
            val str = getLine(i)
            canvas?.drawText(
                str,
                paddingStart.toFloat(),
//                if (i == 0) getFirstLineHeight() else
                (i + 1) * getLineHeight(),
                textPaint
            )
        }
        super.onDraw(canvas)
    }

    private fun getLine(index: Int): String {
        if (index >= mLayout.lineCount) return ""
        return mLayout.text.substring(mLayout.getLineStart(index), mLayout.getLineEnd(index))
    }

    fun getLineCount(): Int {
        return mLayout.lineCount
    }

    fun setTextColor(@ColorInt color: Int) {
        textPaint.color = color
        invalidate()
    }

    fun setTextSize(size: Float) {
        textPaint.textSize = size
        invalidate()
    }

    fun setTypeface(typeface: Typeface) {
        textPaint.typeface = typeface
        invalidate()
    }

    fun getFirstLineHeight(): Float {
        return textPaint.textSize * lineSpaceMult + lineSpaceExtra / 2
    }

    fun getLineHeight(): Float {
        return textPaint.textSize * lineSpaceMult + lineSpaceExtra
    }
}