package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.Book
import com.novel.qingwen.net.bean.BookStoreItem
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.utils.NetUtil

class BookStoreVM:BaseVM() ,ResponseCallback<BookStoreItem>{
    private val list = ArrayList<Book>()
    private var hasNext = false
    private var categoryId:Int = 1
    private var status:String = "new"
    private var page:Int = 1
    private var isLoading = false

    init {
        NetUtil.setCategory(this)
    }

    fun getList():ArrayList<Book> = list

    fun hasNext():Boolean = hasNext

    /**
     * @param page  从1开始
     */
    fun getContent(categoryId:Int,status:String,page:Int = 1){
        this.categoryId = categoryId
        this.status = status
        this.page = page
        isLoading = true
        NetUtil.getCategory(categoryId, status, page)
    }

    fun getNextPage(){
        if (!hasNext ){
            iView?.showMsg("没有数据了")
            return
        }
        if(isLoading){
            return
        }
        getContent(categoryId, status,page)
    }

    private var prepareCancel = false
    fun cancel(){
        prepareCancel = true
        NetUtil.cancelGetCategory()
    }

    override fun onFailure() {
        isLoading = false
        if (prepareCancel) {
            prepareCancel = false
            return
        }
        iView?.showMsg("加载错误")
    }

    override fun onSuccess(t: BookStoreItem) {
        isLoading = false
        hasNext = t.data.HasNext
        if (page == 1)
            list.clear()
        list.addAll(t.data.BookList)
        page++
        iView?.onComplete(target2 = 0)
    }
}