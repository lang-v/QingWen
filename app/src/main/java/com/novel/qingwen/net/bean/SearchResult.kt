package com.novel.qingwen.net.bean

data class SearchResult(
    val `data`: List<SearchResultItem>,
    val info: String,
    val status: Int
)

/**
 *  val Author: String,
val CName: String,
val Desc: String,
val Id: Int,
val Img: String,
val Name: String,
val Score: Double
 */
data class SearchResultItem(
    val Author: String,
    val BookStatus: String,
    val CName: String,
    val Desc: String,
    val Id: String,
    val Img: String,
    val LastChapter: String,
    val LastChapterId: String,
    val Name: String,
    val UpdateTime: String
)