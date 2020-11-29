package com.novel.qingwen.net.callback

interface ResponseCallback<T> {
    fun onFailure(o: Any?)
    fun onSuccess(t:T)
}