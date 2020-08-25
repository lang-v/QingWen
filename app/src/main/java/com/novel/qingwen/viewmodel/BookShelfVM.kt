package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.utils.RoomUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookShelfVM:BaseVM() {
    private val list = BookShelfListUtil.getList()

    fun getList():ArrayList<BookInfo> = list

    fun refresh(){
        NetUtil.setInfo(object :ResponseCallback<com.novel.qingwen.net.bean.BookInfo>{
            override fun onFailure() {
            }
            override fun onSuccess(t: com.novel.qingwen.net.bean.BookInfo) {
                list.forEach {
                    if (it.novelId==t.data.Id){
                        if (it.lastChapterId != t.data.LastChapterId){
                            it.lastChapterId = t.data.LastChapterId
                            it.lastChapterName = t.data.LastChapter
                            it.lastUpdateTime = t.data.LastTime
                            it.update = true
                            iView?.onComplete()
                            //写入数据库
                            BookShelfListUtil.update(it)
                        }
                        return@forEach
                    }
                }
            }
        })
        for (bookInfo in list) {
            NetUtil.getBookInfo(bookInfo.novelId)
        }
        iView?.onComplete()
    }
}