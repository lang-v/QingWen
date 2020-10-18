package com.novel.qingwen.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 后期发现小说的章节ID可能重复，所以增加小说id判断
 * 以小说id 和 章节id 作为联合主键
 */
@Entity(tableName = "BookChapter",primaryKeys = ["novelId","chapterId"])
data class Chapter(
    //章节id
    @ColumnInfo(name = "novelId") val novelId:Long,
    //当前小说id
    @ColumnInfo(name = "chapterId") val chapterId: Long,
    //章节名
    @ColumnInfo(name = "chapterName") val name: String,
    //章节内容
    @ColumnInfo(name = "content") val content: String,
    //下一、章
    @ColumnInfo(name="nextChapter") val nid:Long,
    //上一章
    @ColumnInfo(name="preChapter") val pid:Long
)