package com.novel.qingwen.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_info")
data class BookInfo(
    @PrimaryKey val novelId: Long,
    val img:String,
    val status:String,
    var update:Boolean=false,
    var lastReadId: Long,
    //当前阅读位置在小说章节中的第几页
    var lastReadOffset:Int,
    val novelName: String,
    val firstChapterId:Long,
    var lastChapterId: Long,
    var lastUpdateTime: String,
    var lastChapterName: String
)