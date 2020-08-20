package com.novel.qingwen.net.bean

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
    val FirstChapterId: Long,
    val Id: Long,
    val Img: String,
    val LastChapter: String,
    val LastChapterId: Long,
    val LastTime: String,
    val Name: String
)

data class BookVote(
    val BookId: Int,
    val Score: Double,
    val TotalScore: Int,
    val VoterCount: Int
)