package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.NetUtil

class BookShelfVM : BaseVM() {
    private val list = BookShelfListUtil.getList()
    fun getList(): ArrayList<BookInfo> = list
    fun refresh() {
        BookShelfListUtil.pullData {
            BookShelfListUtil.pushData()
            iView?.onComplete()
        }
        if (list.size == 0 || (list.size == 1 && list[0].novelId == -1L)) {
            iView?.onComplete()
            return
        }
        var size = 0
        NetUtil.setInfo(object : ResponseCallback<com.novel.qingwen.net.bean.BookInfo> {
            override fun onFailure() {
            }

            override fun onSuccess(t: com.novel.qingwen.net.bean.BookInfo) {
                size++
                synchronized(list) {
                    list.forEach {
                        if (it.novelId == t.data.Id) {
                            if (it.lastChapterId != t.data.LastChapterId) {
                                it.lastChapterId = t.data.LastChapterId
                                it.lastChapterName = t.data.LastChapter
                                it.lastUpdateTime = t.data.LastTime
                                it.update = true
                                BookShelfListUtil.update(it)
                            }
                        }
                    }
                    if (size == list.size)
                        iView?.onComplete()
                }
            }
        })
        for (bookInfo in list) {
            NetUtil.getBookInfo(bookInfo.novelId)
        }
    }

}