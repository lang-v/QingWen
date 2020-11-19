package com.novel.qingwen.base

interface IBaseView {
    fun showMsg(msg:String)
    fun onComplete(target: Int = 2, target2: Int)
}