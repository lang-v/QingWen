package com.novel.qingwen.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.novel.qingwen.R

/**
 * 自定义toast布局
 * logo+text
 */
object Show{

    const val NONE:Int = 0
    const val ERROR:Int = 1
    const val RIGHT:Int = 2
    /**
     * @param status 0： 1：错误❌  2：正确√
     * @see NONE 不显示图片
     * @see ERROR 错误图片
     * @see RIGHT 正确图片
     *
     */
    fun show(context:Context,msg:String,status:Int=0){
        val view = (context as Activity).layoutInflater.inflate(R.layout.show_layout,null)
        val text:TextView = view.findViewById(R.id.showMsg)
        val img:ImageView = view.findViewById(R.id.showImg)
        text.text = msg
        when(status){
            0->{
                img.visibility = View.GONE
            }
            1->{
                img.setImageResource(R.drawable.ic_error)
            }
            2->{
                img.setImageResource(R.drawable.ic_right)
            }
        }
        val toast = Toast(context)
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER,0,0)
        toast.show()
    }

}