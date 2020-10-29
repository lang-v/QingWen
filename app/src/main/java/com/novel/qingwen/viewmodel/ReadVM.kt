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
    private var list = ArrayList<Chapter>()
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

    fun clearContent() {
        list.clear()
    }

    /**
     * 小说文章加载时先从数据库中查找，没有缓存则网络请求,返回结果后将结果写入数据库
     * @param attachStart 是否将加载的小说内容添加到集合头部
     * @param count 加载几个章节 1：仅当前章节 2：当前章节和下一章 3：当前章节、上一章和下一章
     */
    fun getChapter(chapterId: Long, attachStart: Boolean = false, slient: Boolean = false) {
        GlobalScope.launch {
            val t = RoomUtil.chapterDao.loadById(novelId, chapterId)
            if (t != null) {
                if (t.content.length < 200 || t.nid == -1L) {
                    RoomUtil.chapterDao.delete(t)
                    getChapter(chapterId, attachStart, slient)
                    return@launch
                }
                if (attachStart) {
                    list.add(0, t)
                    iView?.onComplete(1)
                } else {
                    list.add(t)
                    iView?.onComplete(2)
                }
                return@launch
            }
            this@ReadVM.attachStart = attachStart
            NetUtil.getChapterContent(novelId, chapterId, slient)
        }
    }

    //静默加载
    fun prepareChapter(position: Int) {
        if (position == 0 && list[0].pid != -1L)
            getChapter(list[0].pid, true, slient = true)
        else {
            val chapterId: Long = list[list.size - 1].nid
            getChapter(chapterId, false, slient = true)
        }
    }

    //取消预加载
    fun cancelPrepare() {
        NetUtil.cancelLoadChapter()
    }

    override fun onFailure() {
        iView?.showMsg("加载失败，请检查网络。")
    }

    override fun onSuccess(t: ChapterContent) {
        //避免重复加载
        if (list.size != 0 && list[list.size - 1].chapterId == t.data.cid) return
        val chapter =
            Chapter(t.data.id, t.data.cid, t.data.cname, t.data.content, t.data.nid, t.data.pid)
        if (attachStart) {
            if (chapter.chapterId != list[0].chapterId && chapter.chapterId == list[0].pid) {
                list.add(0, chapter)
                attachStart = false
                iView?.onComplete(1)
            }
        } else {
            if (list.size > 0 && chapter.chapterId != list[list.size - 1].chapterId && chapter.chapterId == list[list.size - 1].nid) {
                list.add(chapter)
                iView?.onComplete(
                    if (list.size == 1) 3
                    else 2
                )
            }
        }
        GlobalScope.launch {
//            Log.e("SQLite","$t")
            val temp = RoomUtil.chapterDao.loadById(chapter.novelId, chapter.chapterId)
            //如果数据库中已经存在此章节，则更新内容
            if (temp != null) {
                RoomUtil.chapterDao.update(
                    chapter.novelId,
                    chapter.chapterId,
                    chapter.content,
                    chapter.nid,
                    chapter.pid
                )
            } else {
                RoomUtil.chapterDao.insertAll(chapter)
            }
        }
    }
}