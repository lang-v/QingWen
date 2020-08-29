package com.novel.qingwen.utils

import com.novel.qingwen.room.entity.BookInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object BookShelfListUtil {
    private val list = ArrayList<BookInfo>()
    private val bookInfoDao = RoomUtil.bookInfoDao
    var currentBookInfo:BookInfo? = null
    fun init() {
        GlobalScope.launch {
            list.addAll(bookInfoDao.loadAll())
        }
    }

    fun getList(): ArrayList<BookInfo> = list

    fun insert(bookInfo: BookInfo) {
        list.forEach {
            if(it.novelId == bookInfo.novelId)return
        }
        list.add(0, bookInfo)
        GlobalScope.launch {
            bookInfoDao.insert(bookInfo)
        }
    }

    fun update(new: BookInfo) {
        GlobalScope.launch {
            for (bookInfo in list) {
                if (bookInfo.novelId == new.novelId) {
                    bookInfo.update = new.update
                    bookInfo.lastReadId = new.lastReadId
                    bookInfo.lastReadOffset = new.lastReadOffset
                    bookInfo.lastChapterName = new.lastChapterName
                    bookInfo.lastChapterId = new.lastChapterId
                    bookInfo.lastUpdateTime = new.lastUpdateTime
                    bookInfoDao.update(new)
                    return@launch
                }
            }
        }
    }

    fun refresh() {
        list.clear()
        GlobalScope.launch {
            list.addAll(bookInfoDao.loadAll())
        }
    }
}