package com.novel.qingwen.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.ERROR
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.FINISH
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.PAUSE
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.UPDATE
import com.novel.qingwen.service.DownloadManager
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.widget.MarqueeTextView
import com.novel.qingwen.viewmodel.DownloadVM
import kotlinx.android.synthetic.main.download_item_layout.view.*

class DownloadListAdapter(private val list: List<DownloadVM.DownloadItem>): RecyclerView.Adapter<DownloadListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.download_item_layout,parent,false))
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        val item = list[position]
        if(payloads.contains("change")){
//            if (holder.name.text != item.name)
//                holder.name.text = item.name
            holder.downloadedCount.text = when(item.status){
                FINISH-> {
                    holder.downloadStatus.isActivated = false
                    "下载完成"
                }
                ERROR-> {
                    holder.downloadStatus.isActivated = false
                    "下载错误"
                }
                PAUSE-> {
                    holder.downloadStatus.isActivated = false
                    "暂停"
                }
                UPDATE->{
                    "已下载：${item.downloadedCount}"
                }
                else -> ""
            }
        }else{
            holder.name.text = item.name
            holder.downloadedCount.text = when(item.status){
                FINISH->"下载完成"
                ERROR->"下载错误"
                PAUSE->"暂停"
                UPDATE->"已下载：${item.downloadedCount}"
                else -> ""
            }
        }
        holder.itemView.downloadStatus.setOnClickListener{
            if(!holder.downloadStatus.isActivated){//恢复下载
                DownloadManager.start(holder.itemView.context,item.nid,item.cid,item.name)
                holder.downloadStatus.isActivated = true
            }else{//暂停下载
                DownloadManager.stopAll()
                holder.downloadStatus.isActivated = false
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        holder.downloadedCount.text = when(item.status){
            FINISH->"下载完成"
            ERROR->"下载错误"
            PAUSE->"暂停"
            UPDATE->"已下载：${item.downloadedCount}"
            else -> ""
        }
        holder.itemView.downloadStatus.setOnClickListener{
            if(!holder.downloadStatus.isActivated){//恢复下载
                DownloadManager.start(holder.itemView.context,item.nid,item.cid,item.name)
                holder.downloadStatus.isActivated = true
            }else{//暂停下载
                DownloadManager.stopAll()
                holder.downloadStatus.isActivated = false
            }
        }
    }

    override fun getItemCount(): Int = list.size

    inner class VH(view: View):RecyclerView.ViewHolder(view){
        val name:MarqueeTextView = itemView.findViewById(R.id.novelName)
        val downloadedCount:TextView = itemView.findViewById(R.id.downloadedCount)
        val downloadStatus :ImageButton = itemView.findViewById(R.id.downloadStatus)
    }
}