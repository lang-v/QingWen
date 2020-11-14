package com.novel.qingwen.net.bean

data class LoginResult(
    var avatar: String,
    var code: Int,
    var email: String,
    var msg: String,
    var nick: String,
    val password: String,
    var token: String,
    var username: String
)