package com.novel.qingwen.room.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Chapter")
data class Chapter(
    //主键 当前章节id
    @PrimaryKey @NonNull val id: Long,
    //章节名
    @ColumnInfo(name = "chapterName") val name: String,
    //章节内容
    @ColumnInfo(name = "content") val content: String,
    //下一、章
    @ColumnInfo(name="nextChapter") val nid:Long,
    //上一章
    @ColumnInfo(name="preChapter") val pid:Long
)