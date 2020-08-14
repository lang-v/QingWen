package com.novel.qingwen.application

import android.app.Application
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.utils.RoomUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        //初始化SQLite
        RoomUtil.init(this)
        GlobalScope.launch(Dispatchers.Default) {
            ConfigUtil.init()
        }
//        GlobalScope.launch {
//            RoomUtil.chapterDao.insertAll(Chapter(11L,"001 重生剑客","那一天，我重生了。",-1,-1))
//            val t = RoomUtil.chapterDao.loadById(11L)
//            Log.e("Application","t=$t")
//        }
    }
}