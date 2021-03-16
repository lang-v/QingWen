package com.novel.qingwen.view.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.transition.Explode
import android.transition.Transition
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.transition.Fade
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import androidx.core.app.ActivityOptionsCompat
import com.novel.qingwen.R
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.utils.UserDataUtil
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.beta.download.DownloadListener
import com.tencent.bugly.beta.download.DownloadTask
import kotlinx.coroutines.*

class Welcome : AppCompatActivity() {
    private val animTime = 1000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        setContentView(R.layout.activity_welcome)
        window.exitTransition =
            Fade(Fade.OUT).apply {
            duration=animTime
        }
        enterFullsScreen()
        GlobalScope.launch {
            UserDataUtil.init {
                BookShelfListUtil.init()
                ConfigUtil.init {
                    initBugly()
                    launch {
                        launch(Dispatchers.Main) {
                            startActivity(Intent(this@Welcome, MainActivity::class.java),ActivityOptionsCompat.makeSceneTransitionAnimation(this@Welcome).toBundle())
                            //等待动画执行完毕，关闭欢迎页面
                            delay(animTime)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun enterFullsScreen() {
        // 全屏展示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // 全屏显示，隐藏状态栏
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun initBugly() {
        Bugly.init(applicationContext, "20fec18d0c", false)
        Beta.enableHotfix = false
        Beta.enableNotification = true
    }
}