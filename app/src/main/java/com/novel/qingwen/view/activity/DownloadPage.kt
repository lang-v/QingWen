package com.novel.qingwen.view.activity

import android.content.IntentFilter
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.broadcast.DownloadListener
import com.novel.qingwen.broadcast.DownloadProgressReceiver
import com.novel.qingwen.broadcast.DownloadProgressReceiver.Companion.ACTION
import com.novel.qingwen.service.DownloadManager
import com.novel.qingwen.utils.show
import com.novel.qingwen.utils.showError
import com.novel.qingwen.view.adapter.DownloadListAdapter
import com.novel.qingwen.viewmodel.DownloadVM
import kotlinx.android.synthetic.main.activity_download_page.*
import kotlinx.android.synthetic.main.activity_resume.*

class DownloadPage : AppCompatActivity(),IBaseView{
    private val viewModel:DownloadVM by viewModels()
    private val receiver = DownloadProgressReceiver()
    private val adapter:DownloadListAdapter by lazy {
        DownloadListAdapter(DownloadVM.list.value!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = Color.parseColor("#669900")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_page)
        setSupportActionBar(downloadToobar)
        init()
    }

    private fun init(){
        supportActionBar?.setDisplayShowHomeEnabled(true)
        downloadToobar.setNavigationIcon(R.drawable.ic_arrow_back_gray)
        supportActionBar?.title = "下载管理"
        downloadList.adapter = adapter
        downloadList.layoutManager = LinearLayoutManager(this)
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.download_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, IntentFilter(ACTION))
        receiver.setListener(viewModel)
        viewModel.attachView(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                onBackPressed()
            }
            R.id.startAll->{
                if(item.title == "全部开始"){
                    startAll()
                    item.title = "全部暂停"
                }else {
                    stopAll()
                    item.title = "全部开始"
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun stopAll(){
        DownloadManager.stopAll()
    }

    private fun startAll(){
        DownloadManager.startAll(this)
    }

    override fun onStop() {
        super.onStop()
        receiver.setListener(null)
        unregisterReceiver(receiver)
        viewModel.detachView()
    }

    override fun showMsg(msg: String) {
        showError(msg)
    }

    override fun onComplete(target: Int, target2: Int) {
        adapter.notifyItemChanged(target,"change")
    }
}
