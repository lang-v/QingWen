package com.novel.qingwen.net.callback

interface ResponseCallback<T> {
    fun onFailure()
    fun onSuccess(t:T)
}