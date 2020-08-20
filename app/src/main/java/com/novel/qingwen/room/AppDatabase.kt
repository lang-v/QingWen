package com.novel.qingwen.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novel.qingwen.room.dao.BookInfoDao
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.dao.ConfigDao
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.room.entity.Chapter
import com.novel.qingwen.room.entity.Config

@Database(entities = [Chapter::class,Config::class,BookInfo::class],version = 2)
abstract class AppDatabase:RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun configDao():ConfigDao
    abstract fun bookInfoDao():BookInfoDao
}