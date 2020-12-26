package com.novel.qingwen.viewmodel

import androidx.lifecycle.MutableLiveData
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.broadcast.DownloadListener
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.ERROR
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.FINISH
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.PAUSE
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.UPDATE

class DownloadVM : BaseVM(), DownloadListener {
    companion object {
        val list = MutableLiveData<ArrayList<DownloadItem>>(ArrayList())
    }

    override fun onUpdate(name: String, nid: Long, cid: Long, downloadedCount: Int) {
        for(i in list.value!!.indices){
            val item = list.value!![i]
            if (item.nid == nid) {
                item.status = UPDATE
                item.downloadedCount = downloadedCount
                iView?.onComplete(i, UPDATE)
                return
            }
        }
    }

    /**
     * target1 代表位置
     * target2 代表状态 ERROR 失败 UPDATE 正常 FINISH 下载完成
     */
    override fun onFinish(name: String, nid: Long) {
        for(i in list.value!!.indices){
            val item = list.value!![i]
            if (item.nid == nid) {
                item.status = FINISH
                iView?.onComplete(i, FINISH)
                return
            }
        }
    }

    override fun onError(name: String, nid: Long) {
        for(i in list.value!!.indices){
            val item = list.value!![i]
            if (item.nid == nid) {
                item.status = ERROR
                iView?.onComplete(i, ERROR)
                return
            }
        }
    }

    override fun onPause(name: String, nid: Long) {
        for(i in list.value!!.indices){
            val item = list.value!![i]
            if (item.nid == nid) {
                item.status = ERROR
                iView?.onComplete(i, PAUSE)
                return
            }
        }
    }

    data class DownloadItem(
        var status: Int,
        val name: String,
        var nid: Long,
        var cid: Long,
        var downloadedCount: Int
    )
}