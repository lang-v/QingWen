package com.novel.qingwen.base

import androidx.lifecycle.ViewModel

open class BaseVM: ViewModel() {
    protected var iView :IBaseView? = null
    fun attachView(view:IBaseView){
        iView = view
    }

    fun detachView(){
        iView = null
    }
}