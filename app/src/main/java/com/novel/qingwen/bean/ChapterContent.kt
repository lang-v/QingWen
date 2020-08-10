package com.novel.qingwen.bean

data class ChapterContent(
    val `data`: ChapterData,
    val info: String,
    val status: Int
)

data class ChapterData(
    val cid: Long,
    val cname: String,
    val content: String,
    val hasContent: Int,
    val id: Long,
    val name: String,
    val nid: Long,
    val pid: Long
)