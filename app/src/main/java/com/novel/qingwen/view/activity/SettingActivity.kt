package com.novel.qingwen.view.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.novel.qingwen.R
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.view.dialog.NoticeDialog
import kotlinx.android.synthetic.main.activity_setting.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class SettingActivity : AppCompatActivity(), ConfigUtil.ConfigUpdateListener {

    private val config = ConfigUtil.getConfig()
    private val dialog:NoticeDialog by lazy { NoticeDialog.build(this,"请稍候") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init()
    }

    private fun init() {
        setSupportActionBar(settingToolbar)
        if (supportActionBar != null) {
            //显示返回按钮
            settingToolbar.setNavigationIcon(R.drawable.back_btn_selector)
            supportActionBar!!.title = "设置"
            //动态设置toolbar高度
            val statusHeight = (getStatusHeight() * 0.9).roundToInt()
            settingToolbar.layoutParams.apply {
                height += statusHeight
            }
            settingToolbar.setPadding(0, statusHeight, 0, 0)
        }
        setTranslucentStatus()
        settingTextStyleSpinner.setSelection(config.textStyle)
        settingTextColorSpinner.setSelection(config.textColor)
        settingTextSizeSpinner.setSelection(config.textSize)
        settingBackGroundColorSpinner.setSelection(config.backGround)
    }

    //状态栏高度
    private fun getStatusHeight(): Int {
        return ceil(25 * resources.displayMetrics.density).roundToInt()
    }

    //设置沉浸状态栏
    private fun setTranslucentStatus() {
        val options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = options
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.settingComplete -> {
                updateConfig()
                updateFinish()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateConfig() {
        /**
        <!-- 阅读页面背景颜色-->
        <string-array name="background_color">
        <!--        默认字体颜色为黑色-->
        <item>白</item>
        <item>米黄</item>
        <item>暗黄</item>
        <item>浅绿</item>
        <item>深绿</item>
        <!--        以下颜色将会改变字体颜色为白色-->
        <item>浅灰</item>
        <item>浅黑</item>
        </string-array>*/
        config.textStyle = settingTextStyleSpinner.selectedItemPosition
        config.backGround = settingBackGroundColorSpinner.selectedItemPosition
        config.textSize = settingTextSizeSpinner.selectedItemPosition
        config.textColor = settingTextColorSpinner.selectedItemPosition
        //更新数据到数据库
        ConfigUtil.update(this)
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)
        return true
    }

    override fun updateFinish() {
        if (dialog.isShowing) {
            dialog.dismiss()
            show("设置完成")
            finish()
        }
    }
}