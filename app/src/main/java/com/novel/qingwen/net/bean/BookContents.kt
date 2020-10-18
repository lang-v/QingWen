package com.novel.qingwen.net.bean

data class BookContents(
    val `data`: VolumeData,
    val info: String,
    val status: Int
)

data class VolumeData(
    val id: Long,
    val list: List<Volume>,
    val name: String
)

data class Volume(
    val list: List<BookChapter>,
    val name: String
)

data class BookChapter(
    val hasContent: Int,
    val id: Long,
    val name: String
)