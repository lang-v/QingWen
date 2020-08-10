package com.novel.qingwen.room

import android.content.Context
import androidx.room.Room
import com.novel.qingwen.room.dao.ChapterDao

object RoomUtil {
    private lateinit var db:AppDatabase
    val chapterDao:ChapterDao by lazy { db.chapterDao() }
    fun init(context: Context){
        db = Room.databaseBuilder(context,AppDatabase::class.java,"qingwenebook").build()
    }
}