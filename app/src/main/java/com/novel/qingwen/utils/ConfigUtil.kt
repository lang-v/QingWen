package com.novel.qingwen.utils

import android.graphics.Color
import com.novel.qingwen.room.entity.Config
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ConfigUtil {
    private lateinit var appConfig: Config

    fun init() {
        var temp :Config? = RoomUtil.configDao.loadById()
        if (temp == null){
            temp = Config(0,20, Color.BLACK, -1, Color.WHITE)
            RoomUtil.configDao.insert(temp)//更新数据
        }
        appConfig = temp
    }

    fun getConfig():Config = appConfig

    fun update(){
        GlobalScope.launch {
            RoomUtil.configDao.update(
                appConfig.id,
                appConfig.textSize,
                appConfig.textColor,
                appConfig.textStyle,
                appConfig.backGround
            )
        }
    }
}