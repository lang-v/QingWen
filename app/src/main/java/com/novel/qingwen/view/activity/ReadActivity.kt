package com.novel.qingwen.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.viewmodel.ReadVM
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReadActivity : AppCompatActivity(), IBaseView {
    companion object {
        fun start(
            context: Context,
            novelId: Long,
            chapterId: Long,
            novelName: String,
            status: String
        ) {
            val intent = Intent(context, ReadActivity::class.java)
            intent.putExtra("novelId", novelId)
            intent.putExtra("chapterId", chapterId)
            intent.putExtra("novelName", novelName)
            intent.putExtra("status", status)
            context.startActivity(intent)
        }
    }

    private val novelId: Long by lazy { intent.getLongExtra("novelId", 6734L) }
    private val chapterId: Long by lazy { intent.getLongExtra("chapterId", 3284642L) }
    private val novelName: String by lazy { intent.getStringExtra("novelName") }
    private val status: String by lazy { intent.getStringExtra("status") }

    private val viewModel: ReadVM by viewModels()
    private val adapter: ReadListAdapter by lazy { ReadListAdapter(viewModel.getList()) }
    private val manager = LinearLayoutManager(this)
    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        dialog.show()
        init()
        //设置小说id,后面才能根据此id获取章节内容
        viewModel.init(novelId)
        //开始加载小说内容,这个方法是异步的,确认不是Activity重建后，开始加载数据
        if (savedInstanceState == null || savedInstanceState.getLong("chapterId", -1L) == -1L) {
            viewModel.getChapter(chapterId)
        }
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("chapterId", chapterId)
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    private fun init() {
        setSupportActionBar(readToolbar)
//        if (supportActionBar != null) {
//            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//            supportActionBar!!.setHomeButtonEnabled(true)
//            supportActionBar!!.title = novelName
//            readToolbar.setNavigationIcon(R.drawable.back_btn_selector)
//        }

        readList.adapter = adapter
        readList.layoutManager = manager
        readList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                manager.findFirstVisibleItemPosition()
                super.onScrolled(recyclerView, dx, dy)
            }

            //加载上一章 下一章
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (manager.findLastVisibleItemPosition() == (adapter.itemCount - 1)//屏幕最下面的完全可见的item是list中的最后一个
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                    && !readList.canScrollVertically(1) //recyclerview的内容足够滑动时
                ) {
                    val list = viewModel.getList()
                    if (list.size == 0) {
                        showError("未知错误。")
                        return
                    }

                    val nid = list[list.size - 1].nid
                    if (nid == -1L) {
                        if (status == "状态：完结")
                            showError("恭喜你又读完一本书。")
                        else
                            showError("已经是最后一章了。")
                        return
                    }
                    //加载下一章
                    viewModel.getChapter(nid)
                    dialog.show()
                    super.onScrollStateChanged(recyclerView, newState)
                    return
                }
                if (manager.findFirstVisibleItemPosition() == 0
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                    && !readList.canScrollVertically(-1)
                ) {
                    val list = viewModel.getList()
                    if (list.size == 0) {
                        showError("未知错误。")
                        return
                    }
                    val pid = list[0].pid
                    if (pid == -1L) {
                        showError("这是第一章哦")
                        return
                    }
                    //加载上一张
                    viewModel.getChapter(pid, true)
                    dialog.show()
                    super.onScrollStateChanged(recyclerView, newState)
                    return
                }
            }
        })

    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (dialog.isShowing)
            dialog.dismiss()
    }

    override fun onComplete(target: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            if (target == 1) {
                adapter.notifyItemInserted(0)
            } else if (target == 2) {
                adapter.notifyItemInserted(adapter.itemCount)
            }
            if (dialog.isShowing)
                dialog.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //返回
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)

    }
}