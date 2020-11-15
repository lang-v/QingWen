package com.novel.qingwen.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.novel.qingwen.R
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.utils.UserDataUtil
import com.tencent.bugly.Bugly
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Welcome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        GlobalScope.launch{
            ConfigUtil.init()
            UserDataUtil.init()
            BookShelfListUtil.init()
            initBugly()
            delay(2000)
            runOnUiThread {
                startActivity(Intent(this@Welcome,MainActivity::class.java))
                finish()
            }
        }
    }

    private fun initBugly(){
        Bugly.init(applicationContext, "20fec18d0c", false)
    }
}