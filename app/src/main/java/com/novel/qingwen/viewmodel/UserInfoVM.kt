package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.LoginResult
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.utils.UserDataUtil

class UserInfoVM:BaseVM(),ResponseCallback<LoginResult> {

    init {
        NetUtil.setUserData(this)
    }

    fun update(nick:String,email:String,avatar:String){
        NetUtil.change(UserDataUtil.default.token,nick, email, avatar)
    }

    override fun onFailure() {
        iView?.showMsg("更新失败")
    }

    override fun onSuccess(t: LoginResult) {
        UserDataUtil.default.nick = t.nick
        UserDataUtil.default.email = t.email
        UserDataUtil.default.avatar = t.avatar
        UserDataUtil.update()
        iView?.onComplete()
    }
}