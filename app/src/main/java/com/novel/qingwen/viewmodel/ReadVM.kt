package com.novel.qingwen.viewmodel

import android.util.Log
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.bean.ChapterContent
import com.novel.qingwen.net.NetUtil
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.RoomUtil
import com.novel.qingwen.room.entity.Chapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReadVM : BaseVM(), ResponseCallback<ChapterContent> {
    private val list = ArrayList<Chapter>()
    private var novelId: Long = -1L
    private var attachStart:Boolean = false

    init {
        NetUtil.chapterContentCallback = this
    }

    fun init(novelId: Long) {
        this.novelId = novelId
    }

    fun getList(): ArrayList<Chapter> {
        return list
    }

    /**
     * 小说文章加载时先从数据库中查找，没有缓存则网络请求,返回结果后将结果写入数据库
     * @param attachStart 是否将加载的小说内容添加到集合头部
     */
    fun getChapter(chapterId: Long, attachStart: Boolean = false) {
        GlobalScope.launch {
            val t = RoomUtil.chapterDao.loadById(chapterId)
            if (t != null) {
                if (attachStart) {
                    list.add(0, t)
                    iView?.onComplete(1)
                }
                else {
                    list.add(t)
                    iView?.onComplete(2)
                }
            } else {
                this@ReadVM.attachStart = attachStart
                NetUtil.getChapterContent(novelId, chapterId)
            }
        }
//        list.add(t)
    }


    override fun onFailure() {
        iView?.showMsg("加载失败，请检查网络。")
    }

    override fun onSuccess(t: ChapterContent) {
        val chapter = Chapter(t.data.cid, t.data.cname, t.data.content, t.data.nid, t.data.pid)
        if (attachStart) {
            list.add(0,chapter)
            attachStart = false
            iView?.onComplete(1)
        }else {
            list.add(chapter)
            iView?.onComplete(2)
        }
        GlobalScope.launch {
            RoomUtil.chapterDao.insertAll(chapter)
        }
    }
}