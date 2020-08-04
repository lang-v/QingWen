package com.novel.qingwen.bean

data class ChapterContent(
    val `data`: ChapterData,
    val info: String,
    val status: Int
)

data class ChapterData(
    val cid: Int,
    val cname: String,
    val content: String,
    val hasContent: Int,
    val id: Int,
    val name: String,
    val nid: Int,
    val pid: Int
)