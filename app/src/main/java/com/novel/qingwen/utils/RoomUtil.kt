package com.novel.qingwen.utils

import android.content.Context
import androidx.room.Room
import com.novel.qingwen.room.AppDatabase
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.dao.ConfigDao

object RoomUtil {
    private lateinit var db: AppDatabase
    val chapterDao:ChapterDao by lazy { db.chapterDao() }
    val configDao:ConfigDao by lazy { db.configDao() }
    fun init(context: Context){
        db = Room.databaseBuilder(context,
            AppDatabase::class.java,"qingwenebook").build()
    }
}