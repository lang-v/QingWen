package com.novel.qingwen.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.novel.qingwen.room.entity.BookInfo
import java.io.ByteArrayOutputStream
import java.util.regex.Matcher
import java.util.regex.Pattern


/*
 * 中文转unicode编码英文不转
 */
fun String.encode(int: Int): String {
    val chars = this.toCharArray()
    val unicodeStr = StringBuilder("")
    for (i in chars.indices) {
        if (chars[i].toString().matches(Regex("[\u4e00-\u9fa5]"))) {
            var hexB = Integer.toHexString(chars[i].toInt())
            if (hexB.length <= 2) {
                hexB = "00$hexB"
            }
            if (int == 1)
                unicodeStr.append("\\u")
            else if (int == 2)
                unicodeStr.append("\\\\u")
            unicodeStr.append(hexB)
        } else {
            unicodeStr.append(chars[i])
        }
    }
    return unicodeStr.toString()
}

/**
 * 使用双斜杠
 */
//infix fun String.encode(int: Int): String {
//    var str = this
//    val utfBytes: CharArray = str.toCharArray()
//    var unicodeBytes = ""
//    for (i in utfBytes.indices) {
//        var hexB = Integer.toHexString(utfBytes[i].toInt())
//        if (hexB.length <= 2) {
//            hexB = "00$hexB"
//        }
//        unicodeBytes = "$unicodeBytes\\\\u$hexB"
//    }
//    return unicodeBytes
//}

fun String.decode(int: Int): String {
    val pattern: Pattern =
        if (int == 2)
            Pattern.compile("(\\\\\\\\u(\\p{XDigit}{4}))")
        else
            Pattern.compile("(\\\\u(\\p{XDigit}{4}))")
    val matcher: Matcher = pattern.matcher(this)
    var s: String = this
    var ch: Char
    while (matcher.find()) {
        ch = matcher.group(2).toInt(16).toChar()
        s = s.replace(matcher.group(1), ch.toString() + "")
    }
    return s
}

fun String.toBitmap(): Bitmap? {
    try {
        if (UserDataUtil.default.avatar == null || UserDataUtil.default.avatar == "") return null
        val bytes: ByteArray = Base64.decode(UserDataUtil.default.avatar, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun Bitmap.toBase64(): String? {
    try {
        var baos: ByteArrayOutputStream?
        if (this != null) {
            baos = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            baos.flush()
            baos.close()
            val bitmapBytes: ByteArray = baos.toByteArray()
            return Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun List<BookInfo>.contain(novelId: Long): Boolean {
    this.forEach {
        if(it.novelId == novelId)return true
    }
//    val it = this.iterator()
//    while (it.hasNext()) {
//        val item = it.next()
//        if (item.novelId == novelId) {
//            return true
//        }
//    }
    return false
}

fun Activity.showError(msg: String) {
    Show.show(this, msg, Show.ERROR)
}

fun Activity.showSuccess(msg: String) {
    Show.show(this, msg, Show.RIGHT)
}

fun Activity.show(msg: String) {
    Show.show(this, msg, Show.NONE)
}

