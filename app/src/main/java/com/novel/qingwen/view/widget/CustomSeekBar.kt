package com.novel.qingwen.view.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import com.novel.qingwen.R
import com.novel.qingwen.utils.ConfigUtil
import kotlin.math.roundToInt

class CustomSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatSeekBar(context, attrs, defStyleAttr),SeekBar.OnSeekBarChangeListener {
    //默认为5
    private var size:Int = 5
    private var listener:OnProgressChanged?=null
    init {
        //获取size
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar,defStyleAttr,0)
        val count = a.indexCount
        for (i in 0 until count){
            val index = a.getIndex(i)
            if (index == R.styleable.CustomSeekBar_size){
                size = a.getInt(index,5)
                if (size == 0)
                    throw IllegalArgumentException("CustomSeekBar point size only support positive but size=$size")
            }
        }
        progress = max/(size-1)*((ConfigUtil.getConfig().textSize-5)/5-1)
        setOnSeekBarChangeListener(this)
    }

    fun setOnSeekBarChangedListener(listener: OnProgressChanged){
        this.listener = listener
    }

    override fun setProgress(progress: Int) {
        val temp = max.toFloat()/(size-1)
        val index = (progress/temp).roundToInt()
        val newProgress = index*temp.toInt()
        //使得当前进度条只能在特定位置停留
        super.setProgress(newProgress)
        listener?.onChanged(index+1)
    }

    //在这里监听用户拖动进度条的情况
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        setProgress(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    interface OnProgressChanged{
        /**
         * 0 < index <= size
         */
        fun onChanged(index:Int)
    }
}