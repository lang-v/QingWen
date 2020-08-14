package com.novel.qingwen.view.activity

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.view.widget.CustomSeekBar
import com.novel.qingwen.viewmodel.ReadVM
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

class ReadActivity : AppCompatActivity(), IBaseView ,CustomSeekBar.OnProgressChanged{
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
    private val novelName: String by lazy {
        intent.getStringExtra("novelName")
//        "重生只"
    }
    private val status: String by lazy { intent.getStringExtra("status") }

    private val viewModel: ReadVM by viewModels()
    private val adapter: ReadListAdapter by lazy {
        ReadListAdapter(
            viewModel.getList()
        )
    }
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
            viewModel.getChapter(chapterId)//前中后三章
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.read_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //返回
            android.R.id.home -> {
                finish()
            }
            R.id.readMenuContents -> {
                showSuccess("打开目录")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        setSupportActionBar(readToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
//            readTitle.text = novelName
            supportActionBar!!.title = novelName
            readToolbar.setNavigationIcon(R.drawable.back_btn_selector)
        }
        readTextSizeSeekBar.setOnSeekBarChangedListener(this)
        readHead.textSize = 12f
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        window.statusBarColor = Color.TRANSPARENT
//        window.setTitleColor(Color.TRANSPARENT)
        //设置背景色
        readList.rootView.setBackgroundColor(viewModel.config.backGround)
        readList.adapter = adapter
        readList.layoutManager = manager

        readList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val item = viewModel.getList()[manager.findFirstVisibleItemPosition()]
                readHead.text = item.name
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                Log.e("ReadActivity onScroll","item count = ${adapter.itemCount} ,first position is ${manager.findFirstVisibleItemPosition()} last position is ${manager.findLastVisibleItemPosition()} " +
//                        "now canScrollUp=${readList.canScrollVertically(-1)} canScrollDown=${readList.canScrollVertically(1)}")
                //加载下一章
                if (manager.findLastVisibleItemPosition() == (adapter.itemCount - 1)//屏幕最下面的完全可见的item是list中的最后一个
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                    && !readList.canScrollVertically(1) //recyclerview无法向上滑动时
                ) {
                    val list = viewModel.getList()
                    if (list.size == 0) {
                        showError("未知错误。")
                        return
                    }

                    val nid = list[list.size - 1].nid
                    if (nid == -1L) {
                        if (status == "状态：完结")
                            showSuccess("恭喜你又读完一本书。")
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

                //加载上一章
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
//        readList.setOnClickListener(this)
//        readLayout.setOnClickListener(this)
        var target = false
        var oldX = 0f
        var oldY = 0f
        readList.setOnTouchListener { v, event ->
//            Log.e("SL","readlist ontouch view=$v event=$event")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    target = true
                    oldX = event.x
                    oldY = event.y
//                    Log.e("SL","click")
                }
                MotionEvent.ACTION_UP -> {
                    if (target) {
                        target = false
                        pageOnClick()
                    }
                }
                //如果这个过程移动幅度过大就过滤掉本次点击事件
                else -> {
                    if (abs(event.x - oldX) > 5f
                        || abs(event.y-oldY) > 5f){
                        target = false
                    }
                }
            }
            target
        }

//        readList.setOnClickListener{
//            Log.e("SL","readlist onClick")
//        }

//        readLayout.setOnClickListener {
//            Log.e("SL","readLayout onClick")
//        }


//        readLayout.setOnTouchListener { _, event ->
//            Log.e("SL","readlayout ontouch")
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    target = true
//                    Log.e("SL","click")
//                }
//                MotionEvent.ACTION_UP -> {
//                    if (target) {
//                        target = false
//                        pageOnClick()
//                    }
//                }
//                else -> {
//                    target = false
//                }
//            }
//            target
//        }
    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (dialog.isShowing)
            dialog.dismiss()
    }

    /**
     * @param target 1:插入头部 2:插入尾部
     */
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


    private var isOpen = false
    private fun pageOnClick() {
//        showSuccess("点击")
//        Log.e("SL","y=${readSetting.y}")
        if (isOpen)
            closeSetting()
        else
            openSetting()
    }

    private val topOpen: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readToolbar,
            "y",
            readToolbar.y,
            readToolbar.y + readToolbar.height.toFloat()
        ).setDuration(300)
    }
    private val topClose: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readToolbar, "y", readToolbar.y, readToolbar.y - readToolbar.height
        ).setDuration(300)
    }

    private val bottomOpen: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(readSetting, "y", readSetting.y, readSetting.y - readSetting.height)
            .setDuration(300)
    }
    private val bottomClose: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(readSetting, "y", readSetting.y, readSetting.y + readSetting.height)
            .setDuration(300)
    }
    private val lock = ReentrantLock()

    //打开关闭  设置面板
    private fun openSetting() {
        if (lock.isLocked) {
            topClose.cancel()
            bottomClose.cancel()
//            readToolbar.y = -readToolbar.height.toFloat()
//            readSetting.y = readSetting.y-readSetting.height
            lock.unlock()
        }
        lock.lock()
        isOpen = true
//        readToolbar.y++
//        readSetting.y--
        topOpen.start()
        bottomOpen.start()
//        Log.e("SL","open")
        //translation 该方法使用相对位移
//        ObjectAnimator.ofFloat(
//            readToolbar,
//            "y",
//            readToolbar.y,
//            readToolbar.y + readToolbar.height.toFloat()
//        ).setDuration(300).apply { setAutoCancel(true) }.start()
//        ObjectAnimator.ofFloat(readSetting, "y", readSetting.y, readSetting.y - readSetting.height)
//            .setDuration(300).start()
//            readToolbar.animate().translationY(readToolbar.height.toFloat()).setDuration(500).start()
//            readSetting.animate().translationY(-readSetting.height.toFloat()).setDuration(500).start()
    }

    private fun closeSetting() {
        if (lock.isLocked) {
            topOpen.cancel()
            bottomOpen.cancel()

            lock.unlock()
        }
        lock.lock()
        isOpen = false
//        readToolbar.y--
//        readSetting.y++
        topClose.start()
        bottomClose.start()
//        Log.e("SL","open")
//        ObjectAnimator.ofFloat(readToolbar, "y", readToolbar.y, readToolbar.y - readToolbar.height)
//            .apply { setAutoCancel(true) }.setDuration(300).start()
//        ObjectAnimator.ofFloat(readSetting, "y", readSetting.y, readSetting.y + readSetting.height)
//            .setDuration(300).start()
//            readToolbar.animate().translationY(-readToolbar.height.toFloat()).setDuration(500)
//                .start()
//            readSetting.animate().translationY(readSetting.height.toFloat()).setDuration(500)
//                .start()
    }

    override fun onChanged(index: Int) {
        GlobalScope.launch {
            ConfigUtil.getConfig().textSize = 5 + index * 5
            ConfigUtil.update()
        }
        GlobalScope.launch(Dispatchers.Main) {
            //重绘
            readList.adapter = adapter
            readList.layoutManager = manager
            adapter.notifyDataSetChanged()
        }
    }
}