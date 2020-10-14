package com.novel.qingwen.utils

import android.app.ActionBar
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.novel.qingwen.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PopupWindowUtil {
    fun showPopupWindow(parent: View, block: () -> Unit) {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.popup_window_layout, null)

        val pop =
            PopupWindow(v, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
        pop.isOutsideTouchable = true
        pop.setBackgroundDrawable(ColorDrawable())
        pop.showAtLocation(
            parent,
            Gravity.NO_GRAVITY,
            parent.width/2,
            parent.y.toInt()+parent.height/2+20
        )
        v.setOnClickListener {
            block.invoke()
            pop.dismiss()
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(3000)
            if (pop.isShowing)
                pop.dismiss()
        }
    }
}