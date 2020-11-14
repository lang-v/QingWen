package com.novel.qingwen.utils

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.novel.qingwen.room.AppDatabase
import com.novel.qingwen.room.dao.BookInfoDao
import com.novel.qingwen.room.dao.ChapterDao
import com.novel.qingwen.room.dao.ConfigDao
import com.novel.qingwen.room.dao.UserDataDao

object RoomUtil {
    private lateinit var db: AppDatabase
    val chapterDao:ChapterDao by lazy { db.chapterDao() }
    val configDao:ConfigDao by lazy { db.configDao() }
    val bookInfoDao:BookInfoDao by lazy { db.bookInfoDao() }
    val userDataDao:UserDataDao by lazy { db.userDataDao() }
    fun init(context: Context){
        val mi = object : Migration(1,2) {
            //增加字段
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Config ADD COLUMN auto_scroll_v INTEGER NOT NULL default 0")
            }

        }
        val mi2 = object :Migration(2,3){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("create table UserData(id Integer primary key not null,token TEXT not null,username TEXT not null,nick TEXT not null,password TEXT not null,email TEXT not null,avatar TEXT not null)")
            }
        }
        db = Room.databaseBuilder(context,
            AppDatabase::class.java,"qingwenebook").addMigrations(mi,mi2).build()
    }
}