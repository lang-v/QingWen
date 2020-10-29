package com.novel.qingwen.utils

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.novel.qingwen.room.AppDatabase
import com.novel.qingwen.room.dao.BookInfoDao
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.dao.ConfigDao

object RoomUtil {
    private lateinit var db: AppDatabase
    val chapterDao:ChapterDao by lazy { db.chapterDao() }
    val configDao:ConfigDao by lazy { db.configDao() }
    val bookInfoDao:BookInfoDao by lazy { db.bookInfoDao() }
    fun init(context: Context){
        val mi = object : Migration(1,2) {
            //增加字段
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Config ADD COLUMN auto_scroll_v INTEGER NOT NULL default 0")
            }

        }
        db = Room.databaseBuilder(context,
            AppDatabase::class.java,"qingwenebook").addMigrations(mi).build()
    }
}