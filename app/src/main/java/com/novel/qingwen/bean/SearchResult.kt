package com.novel.qingwen.bean

data class SearchResult(
    val `data`: List<SearchResultItem>,
    val info: String,
    val status: Int
)

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