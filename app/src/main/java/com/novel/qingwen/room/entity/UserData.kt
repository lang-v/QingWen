package com.novel.qingwen.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserData")
data class UserData(
    @PrimaryKey val id:Int = 0,
    @ColumnInfo(name = "token") var token: String,
    @ColumnInfo(name = "username") var username: String,
    @ColumnInfo(name = "nick") var nick: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "avatar") var avatar: String
)