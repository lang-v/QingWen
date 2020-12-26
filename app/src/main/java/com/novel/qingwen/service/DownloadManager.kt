package com.novel.qingwen.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.GsonBuilder
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.ACTION
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.ERROR
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.FINISH
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.PAUSE
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.UPDATE
import com.novel.qingwen.net.bean.ChapterContent
import com.novel.qingwen.net.service.Novel
import com.novel.qingwen.room.entity.Chapter
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.viewmodel.DownloadVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

/**
 * JobIntentService 不会立即执行
 * IntentService 立即执行
 * 缓存整本小说
 */
class DownloadManager : IntentService("DownloadService") {
    companion object {
        //为真就停止下载
        var stop = false

        fun startAll(context: Context) {
            stop = false
            for (downloadItem in DownloadVM.list.value!!) {
                context.startService(
                    Intent(
                        context,
                        com.novel.qingwen.service.DownloadManager::class.java
                    ).apply {
                        putExtra("novelName", downloadItem.name)
                        putExtra("chapterId", downloadItem.cid)
                        putExtra("novelId", downloadItem.nid)
                    })
            }
        }

        fun start(context: Context, nid: Long, cid: Long, nName: String) {
            stop = false
            context.startService(
                Intent(
                    context,
                    com.novel.qingwen.service.DownloadManager::class.java
                ).apply {
                    putExtra("novelName", nName)
                    putExtra("chapterId", cid)
                    putExtra("novelId", nid)
                })
        }

        fun stopAll() {
            stop = true
        }
    }

    private val chapter = Retrofit.Builder()
        .baseUrl("https://downbakxs.pysmei.com")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null || stop) return
        val nName = intent.getStringExtra("novelName")
        var cid = intent.getLongExtra("chapterId", -1L)
        val nid = intent.getLongExtra("novelId", -1L)
        var downloadedCount = 0
        val item = DownloadVM.DownloadItem(UPDATE, nName!!, nid, cid, downloadedCount)
        var target = true
        for (downloadItem in DownloadVM.list.value!!) {
            if (item.nid == downloadItem.nid) {
                target = false
                break
            }
        }
        if (target)
            DownloadVM.list.value!!.add(item)
        while (nid != -1L && cid != -1L && !stop) {
            val chapter = chapterExists(nid, cid)
            if (chapter != null) {//章节已经存在，跳过
                cid = chapter.nid
                downloadedCount++
                sendBroadcast(Intent(ACTION).apply {
                    putExtra("code", UPDATE)
                    putExtra("nName", nName)
                    putExtra("novelId", nid)
                    putExtra("chapterId", chapter.chapterId)
                    putExtra("downloadedCount", downloadedCount)
                })
                continue
            }
//            Thread.sleep(150)
            val content = download(nid, cid)
            if (content != null) {//成功加载，写入数据库
                saveToDataBase(content)
                cid = content.data.nid
                downloadedCount++
                sendBroadcast(Intent(ACTION).apply {
                    putExtra("code", UPDATE)
                    putExtra("nName", nName)
                    putExtra("novelId", nid)
                    putExtra("chapterId", content.data.cid)
                    putExtra("downloadedCount", downloadedCount)
                })
                continue
            }
            //下载出错直接返回
            //send ErrorAction
            sendBroadcast(Intent(ACTION).apply {
                putExtra("code", ERROR)
                putExtra("nName", nName)
                putExtra("novelId", nid)
            })
            return
        }

        sendBroadcast(Intent(ACTION).apply {
            putExtra("code", if (stop) PAUSE else FINISH)
            putExtra("nName", nName)
            putExtra("novelId", nid)
        })
    }

    private fun download(nid: Long, cid: Long): ChapterContent? {
        val request = chapter.create(Novel::class.java)
        var call = request.getChapterContent(nid, cid)
        var errorCount = 0//错误超过五次后自动放弃
        while (errorCount < 6) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    return response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call = request.getChapterContent(nid, cid)
                errorCount++
                Log.e("DownloadManager", "found error reload $errorCount")
                Thread.sleep(5000)
            }
        }
        return null
    }

    private fun saveToDataBase(content: ChapterContent) {
        if (chapterExists(content.data.id, content.data.cid) != null) return
        val chapter =
            ReadListAdapter.Chapter(
                content.data.id,
                content.data.cid,
                content.data.cname,
                content.data.content,
                content.data.nid,
                content.data.pid
            )
        RoomUtil.chapterDao.insertAll(chapter)
    }

    private fun chapterExists(nid: Long, cid: Long): Chapter? {
        return RoomUtil.chapterDao.loadById(nid, cid)
    }

}