package com.novel.qingwen.bean

data class BookInfo(
    val `data`: BookInfoData,
    val info: String,
    val status: Int
)

data class BookInfoData(
    val Author: String,
    val BookStatus: String,
    val BookVote: BookVote,
    val CId: Int,
    val CName: String,
    val Desc: String,
    val FirstChapterId: Int,
    val Id: Int,
    val Img: String,
    val LastChapter: String,
    val LastChapterId: Int,
    val LastTime: String,
    val Name: String
)

data class BookVote(
    val BookId: Int,
    val Score: Double,
    val TotalScore: Int,
    val VoterCount: Int
)