package com.novel.qingwen.viewmodel

import android.util.Log
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.ChapterContent
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.room.entity.Chapter
import com.novel.qingwen.room.entity.Config
import com.novel.qingwen.utils.ConfigUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReadVM : BaseVM(), ResponseCallback<ChapterContent> {
    private val list = ArrayList<Chapter>()
    private var novelId: Long = -1L
    private var attachStart: Boolean = false
    val config: Config = ConfigUtil.getConfig()

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
     * @param count 加载几个章节 1：仅当前章节 2：当前章节和下一章 3：当前章节、上一章和下一章
     */
    fun getChapter(chapterId: Long, attachStart: Boolean = false, count: Int = 2) {
        GlobalScope.launch {
            val t = RoomUtil.chapterDao.loadById(novelId, chapterId)
            if (t != null) {
                if (t.content.length < 200 || t.nid == -1L){
                    RoomUtil.chapterDao.delete(t)
                    getChapter(chapterId, attachStart, count)
                    return@launch
                }
                if (attachStart) {
                    list.add(0, t)
                    iView?.onComplete(1)
                    if (count == 2 && t.pid != -1L) {
                        getChapter(t.pid, attachStart, 1)
                    }
                } else {
                    list.add(t)
                    iView?.onComplete(2) 
                    if (count == 2 && t.nid != -1L) {
                        getChapter(t.nid, attachStart, 1)
                    }
                }
                return@launch
            }
            this@ReadVM.attachStart = attachStart
            NetUtil.getChapterContent(novelId, chapterId)
        }
//        list.add(t)
    }

    override fun onFailure() {
        iView?.showMsg("加载失败，请检查网络。")
    }

    override fun onSuccess(t: ChapterContent) {
        val chapter =
            Chapter(t.data.id, t.data.cid, t.data.cname, t.data.content, t.data.nid, t.data.pid)
        if (attachStart) {
            list.add(0, chapter)
            attachStart = false
            iView?.onComplete(1)
        } else {
            list.add(chapter)
            iView?.onComplete(2)
        }
        GlobalScope.launch {
//            Log.e("SQLite","$t")
            RoomUtil.chapterDao.insertAll(chapter)
        }
    }
}