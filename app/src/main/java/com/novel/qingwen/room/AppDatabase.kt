package com.novel.qingwen.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novel.qingwen.room.dao.BookInfoDao
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.dao.ConfigDao
import com.novel.qingwen.room.dao.UserDataDao
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.room.entity.Chapter
import com.novel.qingwen.room.entity.Config
import com.novel.qingwen.room.entity.UserData

@Database(entities = [Chapter::class,Config::class,BookInfo::class,UserData::class],version = 4,exportSchema = false)
abstract class AppDatabase:RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun configDao():ConfigDao
    abstract fun bookInfoDao():BookInfoDao
    abstract fun userDataDao():UserDataDao
}