package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.NetUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BookShelfVM : BaseVM() {
    private val list = BookShelfListUtil.getList()
    fun getList(): ArrayList<BookInfo> = list
    fun refresh() {
        BookShelfListUtil.pullData {
            iView?.onComplete(target2 = 0)
        }
        if (list.size == 0 || (list.size == 1 && list[0].novelId == -1L)) {
//            Log.e("call","oncomplete")
            iView?.onComplete(target2 = 0)
            return
        }
        var size = 0
        NetUtil.setInfo(object : ResponseCallback<com.novel.qingwen.net.bean.BookInfo> {
            override fun onFailure(o: Any?) {
            }

            override fun onSuccess(t: com.novel.qingwen.net.bean.BookInfo) {
                synchronized(list) {
                    size++
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
                    if (size == list.size) {
                        iView?.onComplete(target2 = 0)
                    }
                }
            }
        })
        for (bookInfo in list) {
            NetUtil.getBookInfo(bookInfo.novelId)
        }

        GlobalScope.launch {
            delay(1000)
            iView?.onComplete(0,0)
        }
    }

}