package com.novel.qingwen.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import com.novel.qingwen.R
import kotlinx.android.synthetic.main.process_dialog_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 简约的dialog  processBar + message 无背景
 */
class NoticeDialog(context: Context) : Dialog(context,R.style.DialogTheme) {

    companion object{
        fun build(context: Context,msg:String):NoticeDialog{
            val dialog = NoticeDialog(context)
            dialog.setMessage(msg)
            return dialog
        }
    }

    private var msg:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.process_dialog_layout)
        //空白处无法取消
        setCancelable(false)
        setOnKeyListener { dialog, keyCode, _ ->
            var target = false
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                target = true
                dialog.dismiss()
            }
            target
        }
    }

    fun setMessage(msg:String){
        this.msg = msg
        GlobalScope.launch(Dispatchers.Main){
            dialogMsg.text = msg
        }
    }


}