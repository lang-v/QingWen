package com.novel.qingwen.bean

data class BookContents(
    val `data`: VolumeData,
    val info: String,
    val status: Int
)

data class VolumeData(
    val id: Int,
    val list: List<Volume>,
    val name: String
)

data class Volume(
    val list: List<Chapter>,
    val name: String
)

data class Chapter(
    val hasContent: Int,
    val id: Int,
    val name: String
)