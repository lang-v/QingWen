package com.novel.qingwen.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.novel.qingwen.net.bean.LoginResult
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.entity.UserData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserDataUtil {
    lateinit var default: UserData
    fun init(block:(()->Unit)?=null) {
        var temp = RoomUtil.userDataDao.loadById()
        if (temp == null) {
            temp = UserData(0, "", "", "", "", "", "")
            RoomUtil.userDataDao.insert(temp)
        }
        default = temp
        block?.invoke()
//        autoLogin()
    }

    fun isLogin():Boolean{
        return default.token != ""
    }

    private var isWaitForRefresh = false
    get() {
        return if (field) {
            field = false
            true
        }else
            false
    }
    //通知我的页面刷新内容
    fun isWaitRefresh() = isWaitForRefresh

    fun autoLogin(){
        if (!isLogin())return
        NetUtil.setUserData(object :ResponseCallback<LoginResult>{
            override fun onFailure(o: Any?) {}
            override fun onSuccess(t: LoginResult) {
                default.apply {
                    nick = t.nick
                    email = t.email
                    avatar = t.avatar
                }
                update(object :ConfigUtil.RoomUpdateListener{
                    override fun updateFinish() {
                        isWaitForRefresh = true
                    }
                })
            }
        })
        NetUtil.login(default.username, default.password)
    }

    fun getAvatar(): Bitmap? {
        if (default.avatar == null || default.avatar == "") return null
        val bytes: ByteArray = Base64.decode(default.avatar, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //更新完毕发送通知
    fun update(listener: ConfigUtil.RoomUpdateListener? =null) {
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