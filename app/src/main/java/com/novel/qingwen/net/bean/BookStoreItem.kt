package com.novel.qingwen.net.bean

data class BookStoreItem(
    val data: Data,
    val info: String,
    val status: Int
)

data class Data(
    val BookList: List<Book>,
    val HasNext: Boolean,
    val Page: Int
)

data class Book(
    val Author: String,
    val CName: String,
    val Desc: String,
    val Id: Int,
    val Img: String,
    val Name: String,
    val Score: Double
)