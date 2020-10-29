package com.novel.qingwen.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.novel.qingwen.room.dao.BookInfoDao
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.dao.ConfigDao
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.room.entity.Chapter
import com.novel.qingwen.room.entity.Config
import com.tencent.bugly.Bugly.applicationContext

@Database(entities = [Chapter::class,Config::class,BookInfo::class],version = 2,exportSchema = false)
abstract class AppDatabase:RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun configDao():ConfigDao
    abstract fun bookInfoDao():BookInfoDao
}