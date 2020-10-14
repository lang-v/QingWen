package com.novel.qingwen.utils

import com.novel.qingwen.room.entity.BookInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object BookShelfListUtil {
    private val list = ArrayList<BookInfo>()
    private val bookInfoDao = RoomUtil.bookInfoDao
    var currentBookInfo: BookInfo? = null
    fun init() {
        GlobalScope.launch {
            list.addAll(bookInfoDao.loadAll())
        }
    }

    fun getList(): ArrayList<BookInfo> = list

    fun insert(bookInfo: BookInfo) {
        if (list.contains(bookInfo)) return
        GlobalScope.launch {
            synchronized(list) {
                if (bookInfoDao.contain(bookInfo.novelId).size == 1) return@launch
                bookInfoDao.insert(bookInfo)
                list.add(0, bookInfo)
            }
        }
    }

    fun remove(item: BookInfo) {
        synchronized(list) {
            if (list.contains(item)) {
                list.remove(item)
                GlobalScope.launch {
                    if (bookInfoDao.contain(item.novelId).size != 1) return@launch
                    bookInfoDao.delete(item)
                }
            }
        }
    }

    fun update(new: BookInfo) {
        GlobalScope.launch {
            synchronized(list) {
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
    }

    fun refresh(action: (() -> Unit)? = null) {
        GlobalScope.launch {
            synchronized(list) {
                list.clear()
                list.addAll(bookInfoDao.loadAll())
            }
            action?.invoke()
        }
    }
}