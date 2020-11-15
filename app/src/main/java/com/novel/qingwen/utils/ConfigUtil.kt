package com.novel.qingwen.utils

import android.graphics.Color
import com.novel.qingwen.R
import com.novel.qingwen.room.entity.Config
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ConfigUtil {
    private lateinit var appConfig: Config

    fun init() {
        var temp :Config? = RoomUtil.configDao.loadById()
        if (temp == null){
            //默认字体大小20 字体黑色 系统默认字体 背景米白色
            temp = Config(0,2, 0, 0, 0,50)
            RoomUtil.configDao.insert(temp)//更新数据
        }
        appConfig = temp
    }

    fun getConfig():Config = appConfig

    fun getTextStyle():Int{
        return getTextStyle(appConfig.textStyle)
    }
    fun getTextStyle(position: Int):Int{
        return 0
        //删除字体 apk包的一半大小都用来存字体了
//        return when(position){
//            0 -> 0
//            1 -> R.font.ht
//            2 -> R.font.kt
//            else -> 0
//        }
    }

    fun getTextSize():Int{
        return getTextSize(appConfig.textSize)
    }
    fun getTextSize(position: Int):Int{
        return position *5 + 10
    }

    fun getTextColor():Int{
        return getTextColor(appConfig.textColor)
    }
    fun getTextColor(position: Int):Int{
        return if (position == 0)
            Color.BLACK
        else Color.WHITE
    }

    fun getBackgroundColor():Int{
        return getBackgroundColor(appConfig.backGround)
    }

    fun getBackgroundColor(position:Int):Int{
        return Color.parseColor(
            when (position) {
                0 -> "#FFFAF0"
//                0 -> "#006400"
                1 -> "#FFEBCD"
                2 -> "#FFDEAD"
                3 -> "#F0FFF0"
                4 -> "#006400"
                5 -> "#D3D3D3"
                6 -> "#4b5762"
                else -> "#ffffff"
            }
        )
    }

    fun update(listener: RoomUpdateListener? = null){
        GlobalScope.launch {
            RoomUtil.configDao.update(
                appConfig.id,
                appConfig.textSize,
                appConfig.textColor,
                appConfig.textStyle,
                appConfig.backGround,
                appConfig.autoScrollV
            )
            listener?.updateFinish()
        }
    }

    interface RoomUpdateListener {
        fun updateFinish()
    }
}