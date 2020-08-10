package com.novel.qingwen.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.entity.Chapter

@Database(entities = [Chapter::class],version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
}