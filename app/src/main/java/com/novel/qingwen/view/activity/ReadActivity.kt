package com.novel.qingwen.view.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.view.adapter.BookContentsListAdapter
import com.novel.qingwen.view.adapter.ItemOnClickListener
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.view.widget.CustomSeekBar
import com.novel.qingwen.viewmodel.ContentsVM
import com.novel.qingwen.viewmodel.ReadVM
import kotlinx.android.synthetic.main.activity_contents.*
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

//todo 交互优化
class ReadActivity : AppCompatActivity(), IBaseView, CustomSeekBar.OnProgressChanged,
    ItemOnClickListener {
    companion object {
        const val REQCODE = 100
        fun start(
            context: Context,
            novelId: Long,
            chapterId: Long,
            novelName: String,
            status: String,
            isInBookShelf: Boolean = false,
            forResult: Boolean = false
        ) {
            val intent = Intent(context, ReadActivity::class.java)
            intent.putExtra("novelId", novelId)
            intent.putExtra("chapterId", chapterId)
            intent.putExtra("novelName", novelName)
            intent.putExtra("status", status)
            intent.putExtra("isInBookShelf", isInBookShelf)
            if (forResult) {
                try {
                    (context as Activity).startActivityForResult(intent, REQCODE)
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                }
            } else {
                context.startActivity(intent)
            }
        }
    }

    private val novelId: Long by lazy { intent.getLongExtra("novelId", 6734L) }
    private val chapterId: Long by lazy { intent.getLongExtra("chapterId", 3284642L) }
    private val novelName: String by lazy { intent.getStringExtra("novelName") }
    private val status: String by lazy { intent.getStringExtra("status") }

    //true 则会获取bookshelf的vm  记录阅读位置
    private val isInBookShelf: Boolean by lazy { intent.getBooleanExtra("isInBookShelf", false) }

    //记录当前阅读位置
    private var currentReadID: Long = -1L

    //内容
    private val contentViewModel: ReadVM by viewModels()
    private val contentAdapter: ReadListAdapter by lazy {
        ReadListAdapter(
            contentViewModel.getList()
        )
    }
    private val contentManager = LinearLayoutManager(this)

    //目录
    private val contentsViewModel: ContentsVM by viewModels()
    private val contentsAdapter: BookContentsListAdapter by lazy {
        BookContentsListAdapter(contentsViewModel.getList(), this)
    }
    private val contentsManager = LinearLayoutManager(this)

    //数据由viewModel保管 数据视图分离
    private val headList by lazy { contentsViewModel.getHeadList() }

    /**记录当前悬浮头显示个是第几个，
     * 悬浮头集合
     * @see headList
     */
    private var currentHeadViewIndex: Int = 0

    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        dialog.show()
        init()
        //设置小说id,后面才能根据此id获取章节内容
        contentViewModel.init(novelId)
        //开始加载小说内容,这个方法是异步的,确认不是Activity重建后，开始加载数据
        if (savedInstanceState == null || savedInstanceState.getLong("chapterId", -1L) == -1L) {
            contentViewModel.getChapter(chapterId)//前中后三章
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
        contentViewModel.attachView(this)
        contentsViewModel.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        contentViewModel.detachView()
        contentsViewModel.detachView()
        //设置返回值
        setResult(REQCODE, Intent().apply { putExtra("currentReadId", currentReadID) })
        if (!isInBookShelf) return
        //如果这本书是已经加入书架了的就更新阅读位置
        BookShelfListUtil.getList().forEach {
            if (it.novelId == novelId) {
                it.lastReadId = currentReadID
                Log.e("SL", "lastReadId=${it.lastChapterId} current=$currentReadID")
                BookShelfListUtil.update(it)
                return@forEach
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.read_menu, menu)
        return true
    }

    @SuppressLint("WrongConstant")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //返回
            android.R.id.home -> {
                setResult(REQCODE, Intent().apply { putExtra("currentReadId", currentReadID) })
                finish()
            }
            R.id.readMenuContents -> {
                showSuccess("打开目录")
//                ContentsActivity.start(this,novelId,novelName,status)
//                finish()
                if (readDrawerLayout.isDrawerOpen(readBottomView)) {
                    readDrawerLayout.openDrawer(readBottomView)
                    if (contentsViewModel.getList().size == 0)
                        contentsViewModel.load(novelId)
                }
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
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        window.statusBarColor = Color.TRANSPARENT
//        window.setTitleColor(Color.TRANSPARENT)
        //设置背景色
        readList.rootView.setBackgroundColor(contentViewModel.config.backGround)
        readList.adapter = contentAdapter
        readList.layoutManager = contentManager
        currentReadID = chapterId
        readList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val item = contentViewModel.getList()[contentManager.findFirstVisibleItemPosition()]
                readHead.text = item.name
                currentReadID = item.chapterId
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                Log.e("ReadActivity onScroll","item count = ${adapter.itemCount} ,first position is ${manager.findFirstVisibleItemPosition()} last position is ${manager.findLastVisibleItemPosition()} " +
//                        "now canScrollUp=${readList.canScrollVertically(-1)} canScrollDown=${readList.canScrollVertically(1)}")
                //加载下一章
                if (contentManager.findLastVisibleItemPosition() == (contentAdapter.itemCount - 1)//屏幕最下面的完全可见的item是list中的最后一个
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                    && !readList.canScrollVertically(1) //recyclerview无法向上滑动时
                ) {
                    nextChapter()
                    super.onScrollStateChanged(recyclerView, newState)
                    return
                }

                //加载上一章
                if (contentManager.findFirstVisibleItemPosition() == 0
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                    && !readList.canScrollVertically(-1)
                ) {
                    preChapter()
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
                        || abs(event.y - oldY) > 5f
                    ) {
                        target = false
                    }
                }
            }
            target
        }
        contentsInit()
    }

    //目录页的初始化
    private fun contentsInit() {
        readContentsList.adapter = contentsAdapter
        readContentsList.layoutManager = contentsManager
        //分割线
        readContentsList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        readContentsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /**
             * dy > 0 手指向上滑动
             * dy < 0 手指向下滑动
             * 现在存在的问题：
             *  快速滑动时，会导致代码无法捕捉到headView的交替
             *  从而出现headView的没有根据滑动变化，或者headView动画过程不完整
             *  Fix
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                //Log.e("CA","current index=$currentHeadViewIndex")
                if (dy > 0) {
                    //最后一个headView没有后继，不用处理
                    if ((headList.size - 1) == currentHeadViewIndex) return
                    //如果没有找到此View，说明还没出现在屏幕内
                    //下一个headView
                    val view =
                        contentsManager.findViewByPosition(headList[currentHeadViewIndex + 1].id.toInt())
                    if (view == null || dy >= headView.height * 2) {
                        //当用户快速滑动时，会导致recyclerView中的headView快速滑过，被，从而导致这里的view为null
                        //重新检测当前headView
                        if (contentsManager.findFirstVisibleItemPosition() >= headList[currentHeadViewIndex + 1].id.toInt()) {
                            readHeadView.y = 0f
                            readHeadView.text = headList[++currentHeadViewIndex].name
                        }
                        return
                    }
                    //当下一个readHeadView(在RecyclerView中)
                    // 滑动到当前readHeadView的底部（readHeadView.height）时，
                    // 当前readHeadView将跟随下一个readHeadView滑动
                    // 这里的view.y获取到的是滑动之后的坐标
                    if (view.y > 0f && view.y < readHeadView.height) {
                        readHeadView.y = view.y - readHeadView.height
                    } else if (view.y <= 0f) {
                        //当下一个readHeadView滑动到当前readHeadView的位置也就是0f处时
                        //发生替换，下一个readHeadView成为当前readHeadView
                        //回到原点
                        readHeadView.y = 0f
                        readHeadView.text = headList[++currentHeadViewIndex].name
                    }
                } else {
                    //第一个readHeadView顺着滑动就行，不用处理
                    if (0 != currentHeadViewIndex) {
                        //当前readHeadView
                        val view =
                            contentsManager.findViewByPosition(headList[currentHeadViewIndex].id.toInt())
                        if (view != null) {
                            if (view.y > 0 && view.y < readHeadView.height) {
                                readHeadView.text = headList[currentHeadViewIndex - 1].name
                                readHeadView.y = view.y - readHeadView.height
                                return
                            } else if (view.y >= readHeadView.height) {
                                readHeadView.text = headList[--currentHeadViewIndex].name
                                readHeadView.y = 0f
                                return
                            }
                        }

                    }

                    //下面是处理情况 ：当手指向上滑动到一半，又向下滑动
                    //此时currentHeadViewIndex还没有变化
                    //最后一个readHeadView不会出现这种情况不考虑
                    //Log.e("CA","index=$currentHeadViewIndex size=${headList.size-1}")
                    if (currentHeadViewIndex == (headList.size - 1)) return
                    val view2 =
                        contentsManager.findViewByPosition(headList[currentHeadViewIndex + 1].id.toInt())
                            ?: return
                    if (view2.y > 0f && view2.y < readHeadView.height) {
                        readHeadView.y = view2.y - readHeadView.height
                    } else if (view2.y >= readHeadView.height) {
                        //回到原位
                        readHeadView.y = 0f
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        contentsAdapter.notifyDataSetChanged()
    }

    private inline fun preChapter() {
        val list = contentViewModel.getList()
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
        contentViewModel.getChapter(pid, true)
        dialog.show()
    }

    private inline fun nextChapter() {
        val list = contentViewModel.getList()
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
        contentViewModel.getChapter(nid)
        dialog.show()
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
            when (target) {
                1 -> {
                    contentAdapter.notifyItemInserted(0)
                }
                2 -> {
                    contentAdapter.notifyItemInserted(contentAdapter.itemCount)
                }
                0 -> {
                    contentsAdapter.notifyDataSetChanged()
                    if (headList.size != 0) {
                        readHeadView.visibility = View.VISIBLE
                        readHeadView.text = headList[0].name
                    }
                }
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
            lock.unlock()
        }
        lock.lock()
        isOpen = true
        topOpen.start()
        bottomOpen.start()
    }

    private fun closeSetting() {
        if (lock.isLocked) {
            topOpen.cancel()
            bottomOpen.cancel()
            lock.unlock()
        }
        lock.lock()
        isOpen = false
        topClose.start()
        bottomClose.start()
    }

    //修改了字体 或者大小
    override fun onChanged(index: Int) {
        GlobalScope.launch {
            ConfigUtil.getConfig().textSize = 5 + index * 5
            ConfigUtil.update()
        }
        GlobalScope.launch(Dispatchers.Main) {
            //重绘
            readList.adapter = contentAdapter
            readList.layoutManager = contentManager
            contentAdapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        setResult(REQCODE, Intent().apply { putExtra("currentReadId", currentReadID) })
        super.onBackPressed()
    }


    //目录页选中
    override fun onClick(item: ContentsVM.ContentsInfo) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                readList.scrollBy(0, -readList.height)
                if (!readList.canScrollVertically(-1)){
                    preChapter()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                readList.scrollBy(0, readList.height)
                if (!readList.canScrollVertically(1)){
                    nextChapter()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}