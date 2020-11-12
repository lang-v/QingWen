package com.novel.qingwen.view.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Point
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.broadcast.BatteryChangeListener
import com.novel.qingwen.broadcast.BatteryChangeReceiver
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.view.adapter.BookContentsListAdapter
import com.novel.qingwen.view.adapter.ItemOnClickListener
import com.novel.qingwen.view.adapter.PageScrollController
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.view.widget.CenterLayoutManager
import com.novel.qingwen.view.widget.CustomLinearLayoutManager
import com.novel.qingwen.view.widget.CustomSeekBar
import com.novel.qingwen.viewmodel.ContentsVM
import com.novel.qingwen.viewmodel.ReadVM
import kotlinx.android.synthetic.main.activity_contents.*
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.android.synthetic.main.activity_read.contentsList
import kotlinx.android.synthetic.main.activity_read.headView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs


class ReadActivity : AppCompatActivity(), IBaseView, CustomSeekBar.OnProgressChanged,
    View.OnClickListener, BatteryChangeListener {
    companion object {
        const val REQCODE = 100
        fun start(
            context: Context,
            novelId: Long,
            chapterId: Long,
            offset: Int,
            novelName: String,
            status: String,
            isInBookShelf: Boolean = false
        ) {
            val intent = Intent(context, ReadActivity::class.java)
            intent.putExtra("novelId", novelId)
            intent.putExtra("chapterId", chapterId)
            intent.putExtra("offset", offset)
            intent.putExtra("novelName", novelName)
            intent.putExtra("status", status)
            intent.putExtra("isInBookShelf", isInBookShelf)
            context.startActivity(intent)
        }
    }

    private val novelId: Long by lazy { intent.getLongExtra("novelId", 6734L) }
    private val chapterId: Long by lazy { intent.getLongExtra("chapterId", 3284642L) }
    private val novelName: String by lazy {
        intent.getStringExtra("novelName")!!
    }
    private val status: String by lazy { intent.getStringExtra("status")!! }

    //true 则会获取bookshelf的vm  记录阅读位置
    private val isInBookShelf: Boolean by lazy { intent.getBooleanExtra("isInBookShelf", false) }

    //记录当前阅读位置
    private var currentReadID: Long = -1L
    private var readOffset: Int = 0

    //内容
    private val contentViewModel: ReadVM by viewModels()
    private lateinit var contentAdapter: ReadListAdapter
    private var contentManager =
        CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }

    //显示电量
    private val broadcastReceiver = BatteryChangeReceiver()

    //记录是否在等待刷新布局
    private var waitForRefresh = false

    private var firstRun: Boolean = true

    override fun onResume() {
        registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_read)
        init()
        if (dialog.isShowing) {
            dialog.dismiss()
        }
        //设置小说id,后面才能根据此id获取章节内容
        contentViewModel.init(novelId)
        contentViewModel.attachView(this)
        //开始加载小说内容,这个方法是异步的,确认不是Activity重建后，开始加载数据
        if (savedInstanceState == null || savedInstanceState.getLong("chapterId", -1L) == -1L) {
            dialog.show()
            contentViewModel.getChapter(chapterId, false)
        }
    }


    //实时修改电量显示
    private fun checkBattery(level: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            readBattery.background = getDrawable(
                when (level / 20) {
                    0 -> R.drawable.ic_battery_one
                    1 -> R.drawable.ic_battery_two
                    2 -> R.drawable.ic_battery_three
                    3 -> R.drawable.ic_battery_four
                    4 -> R.drawable.ic_battery_five
                    else -> R.drawable.ic_battery_five
                }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("chapterId", chapterId)
    }

    override fun onRestart() {
        if (waitForRefresh) {
            refreshLayout()
            waitForRefresh = false
        }
        super.onRestart()
    }

    override fun onStart() {
        super.onStart()
        contentViewModel.attachView(this)
        PageScrollController.attachView(readList)
        if (PageScrollController.isPause() && !isOpen)
            PageScrollController.resume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // 全屏显示，隐藏状态栏
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }


    //保存阅读进度
    override fun onStop() {
        super.onStop()
        contentViewModel.detachView()
        contentsViewModel.detachView()
        if (PageScrollController.isRunning())
            PageScrollController.pause()
        PageScrollController.detachView()
        //设置返回值
        if (!isInBookShelf) return
        readOffset = if (contentViewModel.getList().size != 0) {
            readList.getChildAt(0).top
        } else {
            return
        }
        //如果这本书是已经加入书架了的就更新阅读位置
        synchronized(BookShelfListUtil.getList()) {
            BookShelfListUtil.getList().forEach {
                if (it.novelId == novelId) {
                    it.lastReadId = currentReadID
                    it.lastReadOffset = readOffset
                    it.update = false
                    BookShelfListUtil.update(it)
                    return@forEach
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.read_menu, menu)
        return true
    }

    private var autoScrollRunning = false

    @SuppressLint("WrongConstant")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //返回
            android.R.id.home -> {
                finish()
            }
            R.id.readMenuContents -> {
                //打开右侧目录
                readDrawerLayout.openDrawer(Gravity.END)
                //选中当前阅读章节所在目录中的item
                selectChapterItem()
            }
            R.id.readAutoScroll -> {
                autoScrollRunning = if (autoScrollRunning) {
                    PageScrollController.stop()
                    item.title = "自动翻页"
                    false
                } else {
                    PageScrollController.start()
                    item.title = "关闭翻页"
                    show("音量键调节滚动速度")
                    true
                }
                if (isOpen)
                    closeSetting()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //将目录滚动到当前阅读章节的位置
    private fun selectChapterItem() {
        kotlin.runCatching {
            val index = contentsAdapter.selected(currentReadID)
            if (index < 0) return
            //让列表更加快速的定位
            contentsManager.scrollToPosition(index)
            contentsManager.smoothScrollToPosition(contentsList, RecyclerView.State(), index)
        }
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
//        readSetting.y += (readSetting.height).toFloat()
        //文字大小进度条设置监听
        readTextSizeSeekBar.setOnSeekBarChangedListener(this)
        //此view继承自TextView 在生成时的所有属性保存在ConfigUtil管理的Config中
        // 所以在xml中无法直接修改其属性，需要代码修改
        readHead.textSize = 12f
        //设置自动阅读滚动速度
        PageScrollController.setV(ConfigUtil.getConfig().autoScrollV)
        //捕捉dialog弹出时的返回按钮
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        //设置属性全屏
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//        window.decorView.systemUiVisibility =
//        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // 全屏显示，隐藏状态栏
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        //获取电量
        val manager =
            getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val tmp = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        checkBattery(tmp)

        //settingBar 的几个按键
        preChapter.setOnClickListener(this)
        nextChapter.setOnClickListener(this)
        moreSetting.setOnClickListener(this)

//        window.statusBarColor = Color.TRANSPARENT
//        window.setTitleColor(Color.TRANSPARENT)
        //时钟
        readClock.setTextColor(ConfigUtil.getTextColor())
        //head
        readHead.setTextColor(ConfigUtil.getTextColor())
        //阅读进度
        readProgress.setTextColor(ConfigUtil.getTextColor())
        //设置背景
        readLayout.setBackgroundColor(ConfigUtil.getBackgroundColor())
        contentAdapter = ReadListAdapter(contentViewModel.getList())
        readList.adapter = contentAdapter
        readList.layoutManager = contentManager
        //不缓存
//        readList.setItemViewCacheSize(0)

        broadcastReceiver.setListener(this)
        currentReadID = chapterId
        if (isInBookShelf) {
            readOffset = intent.getIntExtra("offset", 0)
//            contentManager.scrollToPositionWithOffset(1,readOffset)
        }
        val decimalFormat = DecimalFormat("0")
        readList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val index = contentManager.findFirstVisibleItemPosition()
                if (contentAdapter.itemCount != 0) {
                    val tempItem = readList.getChildAt(0)
                    readProgress.text =
                        decimalFormat.format(abs(-tempItem.y / tempItem.height * 100)) + "%"
                }
                if (index in 0 until contentViewModel.getList().size) {
                    val item = contentViewModel.getList()[index]
                    //避免重复加载
                    readHead.text = item.name
                    if (item.chapterId == currentReadID) return
                    currentReadID = item.chapterId
                    if (index == contentViewModel.getList().size - 1) {
                        if (contentViewModel.getList()[index].nid != -1L) {
                            contentViewModel.cancelPrepare()
                            //提前加载下一章
                            if (!firstRun)
                                contentViewModel.prepareChapter(index)
                        } else {
                            if (autoScrollRunning && !readList.canScrollVertically(1)) {
                                PageScrollController.stop()
                                findViewById<View>(R.id.readAutoScroll).callOnClick()
                                show("阅读结束到底了")
                            }
                        }
                    } else if (index == 0 && contentViewModel.getList()[0].pid != -1L) {
                        if (item.chapterId == currentReadID) return
                        contentViewModel.cancelPrepare()
                        if (!firstRun)
                            contentViewModel.prepareChapter(0)
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //设置界面处于开启状态 不处理滑动加载事件
                if (isOpen) return
                //取消预加载
                contentViewModel.cancelPrepare()
                //加载下一章
                if (contentManager.findLastVisibleItemPosition() == (contentAdapter.itemCount - 1)//屏幕最下面的完全可见的item是list中的最后一个
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                    && !readList.canScrollVertically(1) //recyclerview无法向上滑动时
                ) {
                    if (loadLock && autoScrollRunning){
                        return
                    }
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

        readToolbar.setOnClickListener { _ -> }
        readSetting.setOnClickListener { _ -> }

        var target = false
        var oldX = 0f
        var oldY = 0f
        readList.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //处理所有消息
                    target = true
                    oldX = event.x
                    oldY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    if (target) {
                        target = false
                        pageOnClick()
                    }
                }
                //如果这个过程移动幅度过大就过滤掉本次点击事件
                else -> {
                    //设置界面处于关闭时由readlist处理滑动事件
                    //开启状态由设置界面处理消息
                    if (isOpen) {
                        target = false
                    } else {
                        if (abs(event.x - oldX) > 5f
                            || abs(event.y - oldY) > 5f
                        ) {
                            target = false

                        }
                    }
                }
            }
            target
        }


        //todo 打开设置面板后touch事件接收消息貌似不完整，无法正常处理点击过程中存在滑动的问题
        //todo 本意是如果点击过程滑动则取消click事件，反之则调用click事件
        //当设置被打开时拦截滑动消息
        readList.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return isOpen
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                if (e.action == MotionEvent.ACTION_UP) {
                    closeSetting()
                }
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        contentsInit()
    }
    //自定义  重写了smoothScrollToPosition方法 实现修改滑动时间
//    private val contentsManager = ContentsActivity.MyLinearLayoutManager(this)
    private val contentsManager = CenterLayoutManager(this)
    private lateinit var contentsAdapter: BookContentsListAdapter
    private val contentsViewModel: ContentsVM by viewModels()

    /**记录当前悬浮头显示个是第几个，
     * 悬浮头集合
     * @see headList
     */
    private var currentHeadViewIndex: Int = 0

    //数据由viewModel保管 数据视图分离
    private val headList by lazy { contentsViewModel.getHeadList()}

    //初始化右侧的目录页
    private fun contentsInit() {
        GlobalScope.launch {
            //关闭DrawerLayout手势滑动
            readDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            //列表适配器
            contentsAdapter =
                BookContentsListAdapter(contentsViewModel.getList(), object : ItemOnClickListener {
                    override fun onClick(item: ContentsVM.ContentsInfo) {
                        //清空小说缓存列表
                        contentViewModel.clearContent()
                        contentAdapter.notifyDataSetChanged()
                        //清空阅读位置记录
                        readOffset = 0
                        dialog.show()
                        firstRun = true
                        contentViewModel.getChapter(item.id)
                        readDrawerLayout.closeDrawer(Gravity.RIGHT)
                        closeSetting()
                    }
                })
            //分割线
            contentsList.addItemDecoration(
                DividerItemDecoration(
                    this@ReadActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
            contentsList.layoutManager = contentsManager
            contentsList.adapter = contentsAdapter
            contentsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                                headView.y = 0f
                                headView.text = headList[++currentHeadViewIndex].name
                            }
                            return
                        }
                        //当下一个headView(在RecyclerView中)
                        // 滑动到当前headView的底部（headView.height）时，
                        // 当前headView将跟随下一个headView滑动
                        // 这里的view.y获取到的是滑动之后的坐标
                        if (view.y > 0f && view.y < headView.height) {
                            headView.y = view.y - headView.height
                        } else if (view.y <= 0f) {
                            //当下一个headView滑动到当前headView的位置也就是0f处时
                            //发生替换，下一个headView成为当前headView
                            //回到原点
                            headView.y = 0f
                            headView.text = headList[++currentHeadViewIndex].name
                        }
                    } else {
                        //第一个headView顺着滑动就行，不用处理
                        if (0 != currentHeadViewIndex) {
                            //当前headView
                            val view =
                                contentsManager.findViewByPosition(headList[currentHeadViewIndex].id.toInt())
                            if (view != null) {
                                if (view.y > 0 && view.y < headView.height) {
                                    headView.text = headList[currentHeadViewIndex - 1].name
                                    headView.y = view.y - headView.height
                                    return
                                } else if (view.y >= headView.height) {
                                    headView.text = headList[--currentHeadViewIndex].name
                                    headView.y = 0f
                                    return
                                }
                            }

                        }

                        //下面是处理情况 ：当手指向上滑动到一半，又向下滑动
                        //此时currentHeadViewIndex还没有变化
                        //最后一个headView不会出现这种情况不考虑
                        //Log.e("CA","index=$currentHeadViewIndex size=${headList.size-1}")
                        if (currentHeadViewIndex == (headList.size - 1)) return
                        val view2 =
                            contentsManager.findViewByPosition(headList[currentHeadViewIndex + 1].id.toInt())
                                ?: return
                        if (view2.y > 0f && view2.y < headView.height) {
                            headView.y = view2.y - headView.height
                        } else if (view2.y >= headView.height) {
                            //回到原位
                            headView.y = 0f
                        }
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
            contentsViewModel.attachView(object : IBaseView {
                override fun showMsg(msg: String) {
                    show(msg)
                }

                override fun onComplete(target: Int) {
                    GlobalScope.launch(Dispatchers.Main) {
                        contentsAdapter.notifyDataSetChanged()
                        if (headList.size != 0) {
                            headView.visibility = View.VISIBLE
                            headView.text = headList[0].name
                        }

                        //选中当前章节位置
                        selectChapterItem()
                    }
                }
            })
            //滑动到顶部、底部、当前位置
            readTop.setOnClickListener {
                if (headList.size > 0)
                    headView.text = headList[0].name
                if (contentsViewModel.getList().size > 0)
//                    contentsManager.scrollToPosition(0)
                    contentsList.scrollToPosition(0)
//                contentsList.smoothScrollToPosition(0)
            }
            readBottom.setOnClickListener {
                if (headList.size > 0)
                    headView.text = headList[headList.size - 1].name
                if (contentsAdapter.itemCount > 0)
//                    contentsManager.scrollToPosition(contentsAdapter.itemCount - 1)
                    contentsList.scrollToPosition(contentsAdapter.itemCount - 1)
            }
            readLocation.setOnClickListener {
                selectChapterItem()
            }
            contentsViewModel.load(novelId)
        }
    }

    //此锁用于避免短时间内连续加载相同章节
    private var loadLock = false

    private fun preChapter() {
        if (loadLock) return
        loadLock = true
        val list = contentViewModel.getList()
        if (list.size == 0) {
            showError("未知错误。")
            loadLock = false
            return
        }
        val pid = list[0].pid
        if (pid == -1L) {
            loadLock = false
            return
        }
        //加载上一张
        contentViewModel.getChapter(pid, true)
        dialog.show()
    }

    private fun nextChapter() {
        if (loadLock) return
        loadLock = true
        val list = contentViewModel.getList()
        if (list.size == 0) {
            showError("未知错误。")
            loadLock = false
            return
        }

        val nid = list[list.size - 1].nid
        if (nid == -1L) {
            if (status == "状态：完结")
                showSuccess("恭喜你又读完一本书。")
            else {
                loadLock = false
            }
            return
        }
        //加载下一章
        contentViewModel.getChapter(nid)
        dialog.show()
    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (loadLock) {
            loadLock = false
            if (autoScrollRunning) {
                PageScrollController.stop()
                findViewById<TextView>(R.id.readAutoScroll).callOnClick()
                show("阅读结束")
            }
        }
        if (dialog.isShowing)
            dialog.dismiss()
    }

    /**
     * @param target 1:插入头部 2:插入尾部
     */
    override fun onComplete(target: Int) {
        if (loadLock)
            loadLock = false
        GlobalScope.launch(Dispatchers.Main) {
            when (target) {
                1 -> {
                    contentAdapter.notifyItemInserted(0)
                }

                2 -> {
                    contentAdapter.notifyItemInserted(contentAdapter.itemCount - 1)
                }

                3 -> {
                    contentAdapter.notifyItemInserted(contentAdapter.itemCount - 1)
//                    contentAdapter.notifyItemChanged()
                    //contentManager.scrollToPositionWithOffset(0, readOffset)
                    readList.scrollBy(0, -readOffset)
                    //加载上下章节
                    contentViewModel.prepareChapter(0)
                    contentViewModel.prepareChapter(contentAdapter.itemCount - 1)
                    firstRun = false
                }
            }
            if (dialog.isShowing)
                dialog.dismiss()
        }
    }


    private var isOpen = false
    private fun pageOnClick() {
        if (isOpen) {
            closeSetting()
        } else {
            openSetting()
        }
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
        ObjectAnimator.ofFloat(
            readSetting,
            "y",
            readSetting.y,
            readSetting.y - readSetting.height - getNavigationBarHeight()
        )
            .setDuration(300)
    }
    private val bottomClose: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readSetting,
            "y",
            readSetting.y,
            readSetting.y + readSetting.height + getNavigationBarHeight()
        )
            .setDuration(300)
    }

    //获取导航栏高度
    private fun getNavigationBarHeight(): Int {
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        val realSize = Point()
        display.getSize(size)
        display.getRealSize(realSize)
        val resources: Resources = resources
        val resourceId: Int =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val height: Int = resources.getDimensionPixelSize(resourceId)
        //超出系统默认的导航栏高度以上，则认为存在虚拟导航
        return if (realSize.y - size.y > height - 10) {
            height
        } else 0
    }

    private val settingLock = ReentrantLock()

    //打开关闭  设置面板
    private fun openSetting() {
        if (settingLock.isLocked) {
            if (isOpen) return
            topClose.cancel()
            bottomClose.cancel()
            settingLock.unlock()
        }
        settingLock.lock()
        isOpen = true
        if (PageScrollController.isRunning())
            PageScrollController.pause()

        // 非全屏显示，显示状态栏和导航栏
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_VISIBLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        topOpen.start()
        bottomOpen.start()

    }

    private fun closeSetting() {
        if (settingLock.isLocked) {
            if (!isOpen) return
            topOpen.cancel()
            bottomOpen.cancel()
            settingLock.unlock()
        }
        settingLock.lock()
        isOpen = false
        topClose.start()
        bottomClose.start()
        if (PageScrollController.isPause())
            PageScrollController.resume()
        GlobalScope.launch(Dispatchers.Main) {
            delay(150)
            // 全屏展示
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                // 全屏显示，隐藏状态栏
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    //修改了字体 或者大小
    override fun onChanged(index: Int) {
        GlobalScope.launch {
            ConfigUtil.getConfig().textSize = index
            ConfigUtil.update()
        }
        refreshLayout()
    }

    override fun onBackPressed() {
        if (readDrawerLayout.isDrawerOpen(Gravity.END)) {
            readDrawerLayout.closeDrawer(Gravity.END)
            return
        }

        if (isOpen) {
            closeSetting()
            return
        }

        if (PageScrollController.isRunning() || PageScrollController.isPause())
            PageScrollController.stop()

        if (!isInBookShelf) {
            AlertDialog.Builder(ContextThemeWrapper(this, R.style.CommonDialog))
                .setTitle("喜欢这本书吗？")
                .setMessage("加入书架吧！")
                .setPositiveButton(
                    "好的"
                ) { _, _ ->
                    BookShelfListUtil.currentBookInfo?.let {
                        BookShelfListUtil.insert(
                            it
                        )
                    }
                    super.onBackPressed()
                }.setNegativeButton("算了") { _, _ ->
                    super.onBackPressed()
                }.show()
        } else {
            super.onBackPressed()
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isOpen) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (autoScrollRunning) {
                    ConfigUtil.getConfig().autoScrollV = PageScrollController.addV()
                    ConfigUtil.update()
                    return true
                }
                readList.scrollBy(0, -readList.height)
                if (!readList.canScrollVertically(-1)) {
                    preChapter()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (autoScrollRunning) {
                    ConfigUtil.getConfig().autoScrollV = PageScrollController.reduceV()
                    ConfigUtil.update()
                    return true
                }
                readList.scrollBy(0, readList.height)
                if (!readList.canScrollVertically(1)) {
                    nextChapter()
                }
                return true
            }
            //vivo 手机特殊按键
            308 -> {
                findViewById<TextView>(R.id.readAutoScroll).callOnClick()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.moreSetting -> {
                pageOnClick()
                //当返回activity时调用刷新布局
                waitForRefresh = true
                startActivity(Intent(this, SettingActivity::class.java))
            }
            R.id.preChapter -> {
                //关闭SettingBar
                pageOnClick()
                val position = contentManager.findFirstVisibleItemPosition()
                if (position == 0) {
                    preChapter()
                } else {
                    readList.scrollToPosition(position - 1)
                }
            }
            R.id.nextChapter -> {
                pageOnClick()
                val position = contentManager.findFirstVisibleItemPosition()
                if (position == contentAdapter.itemCount - 1) {
                    nextChapter()
                } else {
                    contentManager.scrollToPositionWithOffset(position, -readList.height / 2)
//                    readList.scrollToPosition(position + 1)
                }
            }
        }
    }

    //刷新布局 应用设置
    private fun refreshLayout() {
        GlobalScope.launch(Dispatchers.Main) {
            val position = contentManager.findFirstVisibleItemPosition()
            //列表
            contentAdapter = ReadListAdapter(contentViewModel.getList())
            contentManager = CustomLinearLayoutManager(this@ReadActivity)
//            contentManager.setScrollEnabled(true)
            readList.adapter = contentAdapter
            readList.layoutManager = contentManager
            contentAdapter.notifyDataSetChanged()
//            contentManager.scrollToPositionWithOffset(position, readOffset)
            //时钟
            readClock.setTextColor(ConfigUtil.getTextColor())
            //head
            readHead.setTextColor(ConfigUtil.getTextColor())
            //阅读进度
            readProgress.setTextColor(ConfigUtil.getTextColor())
            //设置背景
            readLayout.setBackgroundColor(ConfigUtil.getBackgroundColor())
        }
    }

    override fun change(level: Int) {
        checkBattery(level)
    }

}