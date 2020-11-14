package com.novel.qingwen.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.novel.qingwen.room.AppDatabase
import com.novel.qingwen.room.entity.UserData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserDataUtil {
    lateinit var default: UserData
    fun init() {
        var temp = RoomUtil.userDataDao.loadById()
        if (temp == null) {
            temp = UserData(0, "", "", "", "", "", "")
            RoomUtil.userDataDao.insert(temp)
        }
        default = temp
    }

    fun isLogin():Boolean{
        return default.token != ""
    }

    fun getAvatar(): Bitmap? {
        if (default.avatar == null || default.avatar == "") return null
        val bytes: ByteArray = Base64.decode(default.avatar, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //更新完毕发送通知
    fun update(listener: ConfigUtil.RoomUpdateListener?=null) {
        GlobalScope.launch {
            RoomUtil.userDataDao.update(
                default.token,
                default.username,
                default.nick,
                default.email,
                default.avatar,
                default.password
            )
            listener?.updateFinish()
        }
    }
}