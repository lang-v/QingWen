package com.novel.qingwen.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.lang.ref.WeakReference

class DownloadProgressReceiver: BroadcastReceiver() {
    private var listener: WeakReference<DownloadListener?> = WeakReference(null)
    companion object{
        const val FINISH = 2
        const val ERROR = 4
        const val UPDATE = 8
        const val PAUSE = 16
        const val ACTION = "con.novel.download"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent==null || listener.get() == null)return
        val code = intent.getIntExtra("code",-1)
        val name = intent.getStringExtra("nName")
        val nid = intent.getLongExtra("novelId",-1L)
        when(code){
            FINISH->{
                listener.get()?.onFinish(name!!,nid)
            }

            ERROR->{
                listener.get()?.onError(name!!,nid)
            }

            PAUSE->{
                listener.get()?.onPause(name!!,nid)
            }

            UPDATE->{
                val count = intent.getIntExtra("downloadedCount",-1)
                val cid = intent.getLongExtra("chapterId",-1L)
                listener.get()?.onUpdate(name!!,nid,cid,count)
            }
        }
    }

    fun setListener(listener: DownloadListener?){
        this.listener = WeakReference(listener)
    }
}

interface DownloadListener{
    /**
     * @param downloadedCount 已经下载了的数量
     */
    fun onUpdate(name:String,nid:Long,cid:Long,downloadedCount:Int)
    fun onFinish(name:String,nid: Long)
    fun onError(name:String,nid:Long)
    fun onPause(name:String,nid:Long)
}