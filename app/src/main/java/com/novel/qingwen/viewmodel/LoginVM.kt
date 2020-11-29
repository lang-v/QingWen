package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.BaseResponse
import com.novel.qingwen.net.bean.LoginResult
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.utils.UserDataUtil

class LoginVM : BaseVM(), ResponseCallback<LoginResult> {
    private val usernameCallback: ResponseCallback<BaseResponse> =
        object : ResponseCallback<BaseResponse> {
            override fun onFailure(o: Any?) {}
            override fun onSuccess(t: BaseResponse) {
                if (t.code == 200)
                    iView?.onComplete(0, 0)
                else
                    iView?.onComplete(1, 0)
            }
        }

    init {
        NetUtil.setUserData(this)
        NetUtil.usernameCallback = usernameCallback
    }

    fun login(username: String, password: String) {
        NetUtil.login(username, password)
    }

    fun register(username: String, password: String) {
        NetUtil.register(username, password)
    }

    fun checkName(username: String) {
        NetUtil.checkName(username)
    }

    override fun onFailure(o: Any?) {
        iView?.showMsg("发生错误")
    }

    override fun onSuccess(t: LoginResult) {
        if (t.code == 200) {
            UserDataUtil.default.avatar = t.avatar
            UserDataUtil.default.email = t.email
            UserDataUtil.default.nick = t.nick
            UserDataUtil.default.username = t.username
            UserDataUtil.default.password = t.password
            UserDataUtil.default.token = t.token
            UserDataUtil.update()
            iView?.onComplete(2, 0)

        } else {
            t.msg?.let { iView?.showMsg(it) }
        }
    }
}