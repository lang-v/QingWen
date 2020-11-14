package com.novel.qingwen.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.novel.qingwen.net.bean.BookShelf
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.entity.BookInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

object BookShelfListUtil {
    private val list = ArrayList<BookInfo>()
    private val bookInfoDao = RoomUtil.bookInfoDao
    var currentBookInfo: BookInfo? = null
    fun init() {
        GlobalScope.launch {
            list.addAll(bookInfoDao.loadAll())
            pullData {
                pushData()
            }
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
                pushData()
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
                    pushData()
                }
            }
        }
    }

    fun update(newBook: BookInfo, needPushData: Boolean = true) {
        GlobalScope.launch {
            synchronized(list) {
                for (bookInfo in list) {
                    if (bookInfo.novelId == newBook.novelId) {
                        bookInfo.update = newBook.update
                        bookInfo.lastReadId = newBook.lastReadId
                        bookInfo.lastReadOffset = newBook.lastReadOffset
                        bookInfo.lastChapterName = newBook.lastChapterName
                        bookInfo.lastChapterId = newBook.lastChapterId
                        bookInfo.lastUpdateTime = newBook.lastUpdateTime
                        bookInfoDao.update(newBook)
                        if (needPushData)
                            pushData()
                        return@launch
                    }
                }
            }
        }
    }

    //上传数据到服务器
    fun pushData() {
//        Log.e("userdata","pushData")
        if (UserDataUtil.isLogin()) {
            if (list.size == 1 && list[0].novelId < 0) return
            NetUtil.pushBookShelf(UserDataUtil.default.token, Gson().toJson(list))
        }
    }

    //从服务器拉取数据
    fun pullData(block: () -> Unit) {
        if (UserDataUtil.isLogin()) {
            NetUtil.setBookShelf(object : ResponseCallback<BookShelf> {
                override fun onFailure() {}
                override fun onSuccess(t: BookShelf) {
                    kotlin.runCatching {
                        if (t.data == null) return@runCatching
                        //利用typetoken 转化json为list对象
                        val temp = Gson().fromJson<ArrayList<BookInfo>>(
                            t.data,
                            object : TypeToken<List<BookInfo>>() {}.type
                        )
                        //此处比较本地和服务器数据，将两者合并
                        temp.forEach {
                            if (!list.contain(it.novelId)) {
                                if (it.novelId > 0) {
                                    list.add(it)
                                    insert(it)
                                }
                            }
                        }
                        block.invoke()
                    }
                }
            })
            NetUtil.pullBookShelf(UserDataUtil.default.token)
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