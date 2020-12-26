package com.novel.qingwen.view.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.broadcast.BatteryChangeListener
import com.novel.qingwen.broadcast.BatteryChangeReceiver
import com.novel.qingwen.utils.*
import com.novel.qingwen.view.adapter.BookContentsListAdapter
import com.novel.qingwen.view.adapter.ItemOnClickListener
import com.novel.qingwen.view.adapter.PageScrollController
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.view.widget.CenterLayoutManager
import com.novel.qingwen.view.widget.CustomSeekBar
import com.novel.qingwen.viewmodel.ContentsVM
import com.novel.qingwen.viewmodel.ReadVM
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private var currentIndex = -1
    private var readOffset: Int = 0

    private var statusBarHeight: Int = 0

    //内容
    private val contentViewModel: ReadVM by viewModels()
    private lateinit var contentAdapter: ReadListAdapter
    private var contentManager =
        CenterLayoutManager(this, ConfigUtil.getDirection(), false)
    private var pagerSnapHelper = PagerSnapHelper()

    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }

    //显示电量
    private val broadcastReceiver = BatteryChangeReceiver()

    //记录是否在等待刷新布局
    private var waitForRefresh = false

    private var firstRun: Boolean = true
    override fun onResume() {
        registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        readList.keepScreenOn = true
        contentViewModel.setNetworkAvailable(isNetworkAvailable())
//        val scale = resources.displayMetrics.density
//        MeasurePage.setWidth((readList.measuredWidth/scale).toInt())
//        MeasurePage.setHeight((readList.measuredHeight/scale).toInt())
        super.onResume()
    }

    //检查网络是否可用
    fun isNetworkAvailable(): Boolean {
        val connectivity = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null && info.isConnected) {       // 当前网络是连接的
            return info.state == NetworkInfo.State.CONNECTED // 当前所连接的网络可用
        }
        return false
    }


    override fun onPause() {
        unregisterReceiver(broadcastReceiver)
        super.onPause()
        readList.keepScreenOn = false
        //保存阅读进度
        if (!isInBookShelf) return
        val position = contentManager.findFirstVisibleItemPosition()
        readOffset = if (position > 0)
            contentViewModel.getList()[position].index
        else -1
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        setContentView(R.layout.activity_read)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
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
            waitForRefresh = false
            refreshLayout()
        }
        super.onRestart()
    }

    override fun onStart() {
        super.onStart()
        contentViewModel.attachView(this)
//        MeasurePage.setHeight(readList.height)
        PageScrollController.attachView(readList)
        if (PageScrollController.isPause() && !isOpen)
            PageScrollController.resume()
        enterFullsScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        PageScrollController.stop()
    }

    //保存阅读进度
    override fun onStop() {
        super.onStop()
        contentViewModel.detachView()
        contentsViewModel.detachView()
        if (PageScrollController.isRunning())
            PageScrollController.pause()
        PageScrollController.detachView()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.read_menu, menu)
        return true
    }

    private var autoScrollRunning = false

    @SuppressLint("WrongConstant")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //缓存全书
            R.id.downloadBook -> {
                show("开始下载")
//                com.novel.qingwen.service.DownloadManager.stop = false
//                DownloadVM.list.value?.add(DownloadVM.DownloadItem(UPDATE,novelName,novelId,BookShelfListUtil.currentBookInfo!!.firstChapterId,0))
//                startService(Intent(this,com.novel.qingwen.service.DownloadManager::class.java).apply {
//                    putExtra("novelName",novelName)
//                    putExtra("novelId",novelId)
//                    putExtra("chapterId",BookShelfListUtil.currentBookInfo!!.firstChapterId)
//                })
                com.novel.qingwen.service.DownloadManager.start(
                    this,
                    novelId,
                    BookShelfListUtil.currentBookInfo!!.firstChapterId,
                    novelName
                )
                item.isEnabled = false
            }
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
                if (ConfigUtil.getDirection() == LinearLayout.VERTICAL || contentViewModel.getList().size > 0) {
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
                    if (isOpen) {
                        //刷新布局后才关闭设置面板
//                    refreshLayout()
                        closeSetting()
                    }
                }
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
        //初始化String分页测量工具
//        MeasurePage.getTextPaint().apply {
//            textSize = ConfigUtil.getTextSize().toFloat()
//            if (ConfigUtil.getTextStyle() != 0)
//                typeface = ResourcesCompat.getFont(this@ReadActivity, ConfigUtil.getTextStyle())
//        }
        measurePageText.setTextSize(ConfigUtil.getTextSize().toFloat())
//        MeasurePage.initView(measurePageText)
//        val layoutParams = readHead.layoutParams as RelativeLayout.LayoutParams
//        layoutParams.bottomMargin = 0
//        readHead.paddingBottom
//        readHead.layoutParams = layoutParams

        //获取状态栏高度
        val resourceId =
            applicationContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        statusBarHeight = if (resourceId > 0) {
            applicationContext.resources.getDimensionPixelSize(resourceId)
        } else
            0
        val layoutParams = readHead.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = statusBarHeight
//        readHead.paddingBottom
        readHead.layoutParams = layoutParams
        readHead.textSize = 12f
        readToolbarTab.layoutParams.apply {
            height += statusBarHeight
            (this as FrameLayout.LayoutParams).topMargin = -height
        }
        readSetting.layoutParams.apply {
            height + getNavigationBarHeight()
            (this as RelativeLayout.LayoutParams).bottomMargin = -height
        }

//        val scale = resources.displayMetrics.density
//        MeasurePage.setHeight((resources.displayMetrics.heightPixels- readHead.height - readFooter.height - statusBarHeight))
//        MeasurePage.setWidth((resources.displayMetrics.widthPixels- 20 * scale).toInt())
        MeasurePage.initView(measurePageText)
//        MeasurePage.getTextPaint().apply {
//            textSize=measurePageText.textSize
//        }
        //设置自动阅读滚动速度
        PageScrollController.setV(ConfigUtil.getConfig().autoScrollV)
        //捕捉dialog弹出时的返回按钮
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        enterFullsScreen()
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
        turnPageCover.setOnClickListener(this)
        turnPageScroll.setOnClickListener(this)

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
        if (ConfigUtil.getDirection() == LinearLayout.HORIZONTAL) {
            pagerSnapHelper.attachToRecyclerView(readList)
            turnPageCover.isSelected = true
            turnPageScroll.isSelected = false
        } else {
            turnPageScroll.isSelected = true
            turnPageCover.isSelected = false
        }
        broadcastReceiver.setListener(this)
        currentReadID = chapterId
        if (isInBookShelf) {
            readOffset = intent.getIntExtra("offset", 0)
//            contentManager.scrollToPositionWithOffset(1,readOffset)
        }
        readList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                currentIndex = contentManager.findFirstVisibleItemPosition()

                val list = contentViewModel.getList()
                if (currentIndex in list.indices) {
                    val item = contentViewModel.getList()[currentIndex]
                    //避免重复加载
                    readHead.text = item.name
                    currentReadID = item.chapterId
                    if (list[currentIndex].nid == -1L) {
                        if (autoScrollRunning && !readList.canScrollVertically(1)) {
                            PageScrollController.stop()
                            findViewById<View>(R.id.readAutoScroll).callOnClick()
                            show("阅读结束到底了")
                        }
                    }
//                    if ((currentIndex == 0 &&list[0].pid != -1L)
//                        || (list[0].chapterId == list[currentIndex].chapterId && list[0].pid != -1L)
//                    ) {
//                        if (item.chapterId == currentReadID) return
//                        if (!firstRun) {
//                            contentViewModel.prepareChapter(0)
//                        }
//                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //设置界面处于开启状态 不处理滑动加载事件
                if (isOpen) return
                currentIndex = contentManager.findFirstVisibleItemPosition()
                if (!firstRun) {
                    contentViewModel.prepareChapter(currentIndex)//后台加载上下两个相邻章节
                }
                if (contentManager.findLastVisibleItemPosition() == (contentAdapter.itemCount - 1)//屏幕最下面的完全可见的item是list中的最后一个
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动 //recyclerview无法向上滑动时
                ) {
                    if (loadLock) {
                        return
                    }

                    if (ConfigUtil.getDirection() == LinearLayout.HORIZONTAL) {
                        if (readList.canScrollHorizontally(1)) return
                    } else {
                        if (readList.canScrollVertically(1)) return
                    }

                    nextChapter()
                    super.onScrollStateChanged(recyclerView, newState)
                    return
                }

                //加载上一章
                if (contentManager.findFirstVisibleItemPosition() == 0
                    && newState == RecyclerView.SCROLL_STATE_IDLE//当前recyclerview停止滑动
                ) {
                    if (loadLock) {
                        return
                    }

                    if (ConfigUtil.getDirection() == LinearLayout.HORIZONTAL) {
                        if (readList.canScrollHorizontally(-1)) return
                    } else {
                        if (readList.canScrollVertically(-1)) return
                    }

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
//                        target = false
                        pageOnClick()
                        target = false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    //设置界面处于关闭时由readlist处理滑动事件
                    //开启状态由设置界面处理消息
                    if (isOpen) {
                        target = false
                    }
                    if (abs(oldX - event.x) > 5f || abs(oldY - event.y) > 5f)
                        target = false
                }
            }
            target
        }

        //当设置被打开时拦截滑动消息
        readList.addOnItemTouchListener(
            object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                Log.e("intercept", "isopen$isOpen")
                    return isOpen
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            oldX = e.x
                            oldY = e.y
                        }
                        MotionEvent.ACTION_UP -> {
                            if (abs(oldX - e.x) < 10f && abs(oldY - e.y) < 10f)
                                closeSetting()
                        }
                    }
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })


        contentsInit()
    }

    //自定义  重写了smoothScrollToPosition方法 实现修改滑动时间
    private val contentsManager = CenterLayoutManager(this)
    private lateinit var contentsAdapter: BookContentsListAdapter
    private val contentsViewModel: ContentsVM by viewModels()

    /**记录当前悬浮头显示个是第几个，
     * 悬浮头集合
     */
    private var currentHeadViewIndex: Int = 0

    //数据由viewModel保管 数据视图分离
    private val headList by lazy { contentsViewModel.getHeadList() }

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

                override fun onComplete(target: Int, target2: Int) {
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
        if (dialog.isShowing) {
            //显示dialog可能会导致全屏退出
            dialog.dismiss()
            enterFullsScreen()
        }
    }

    /**
     * @param target 1:插入头部 2:插入尾部
     */
    override fun onComplete(target: Int, target2: Int) {
        if (loadLock)
            loadLock = false
        GlobalScope.launch(Dispatchers.Main) {
            when (target) {
                1 -> {
                    contentAdapter.notifyItemRangeInserted(0, target2)
                }

                2 -> {
                    contentAdapter.notifyItemRangeInserted(
                        contentAdapter.itemCount - target2,
                        target2
                    )
                }

                3 -> {
                    val list = contentViewModel.getList()
                    synchronized(list) {
                        if (readOffset in 1 until list.size) {
                            val temp = ArrayList<ReadListAdapter.Chapter>()
                            for (i in 0 until readOffset) {
                                temp.add(list[0])
                                list.removeAt(0)
                            }
                            contentAdapter.notifyItemRangeInserted(0, list.size)
//                        contentAdapter.notifyItemRangeInserted(0, target2-readOffset+1)
                            readList.post {
                                for (i in (readOffset - 1) downTo 0) {
                                    list.add(0, temp[i])
                                }
                                contentAdapter.notifyItemRangeInserted(0, readOffset)
                            }
                        } else {
                            contentAdapter.notifyItemRangeInserted(0, target2)
                        }
                        firstRun = false
                    }
                }
            }
            if (dialog.isShowing) {
                dialog.dismiss()
                enterFullsScreen()
            }
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

    //进入全屏
    @Synchronized
    private fun enterFullsScreen() {
        // 全屏展示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.STATUS_BAR_HIDDEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // 全屏显示，隐藏状态栏
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    //退出全屏
    @Synchronized
    private fun exitFullScreen() {
//        // 非全屏显示，显示状态栏和导航栏
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_VISIBLE
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                )
//        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        val options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        window.decorView.systemUiVisibility = options
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_LAYOUT_FLAGS
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.statusBarColor = Color.TRANSPARENT
    }

    private val topOpen: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readToolbarTab,
            "y",
            readToolbarTab.y,
            readToolbarTab.y + readToolbarTab.height
        ).setDuration(300)
    }
    private val topClose: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readToolbarTab,
            "y",
            readToolbarTab.y,
            readToolbarTab.y - readToolbarTab.height
        )
    }
    private val bottomOpen: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readSetting,
            "y",
            readSetting.y,
            readSetting.y - readSetting.height
        )
    }
    private val bottomClose: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            readSetting,
            "y",
            readSetting.y,
            readSetting.y + readSetting.height
        )
    }

    private val open: AnimatorSet by lazy {
        AnimatorSet().apply {
            playTogether(topOpen, bottomOpen)
            duration = 300
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    exitFullScreen()
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                    enterFullsScreen()
                }

                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

    private val close: AnimatorSet by lazy {
        AnimatorSet().apply {
            playTogether(topClose, bottomClose)
            duration = 300
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    enterFullsScreen()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

    private val settingLock = ReentrantLock()


    //打开关闭  设置面板
    private fun openSetting() {
        if (settingLock.isLocked) {
            if (isOpen) return
            close.cancel()
//            topClose.cancel()
//            bottomClose.cancel()
            settingLock.unlock()
        }
        settingLock.lock()
        isOpen = true
        findViewById<TextView>(R.id.readAutoScroll).isEnabled = turnPageScroll.isSelected
        if (PageScrollController.isRunning())
            PageScrollController.pause()

        open.start()
//        exitFullScreen()
//        topOpen.start()
//        bottomOpen.start()
    }

    private fun closeSetting() {
        if (settingLock.isLocked) {
            if (!isOpen) return
            open.cancel()
//            topOpen.cancel()
//            bottomOpen.cancel()
            settingLock.unlock()
        }
        settingLock.lock()
        isOpen = false
//        topClose.start()
//        bottomClose.start()
        close.start()
        if (PageScrollController.isPause())
            PageScrollController.resume()
//        GlobalScope.launch {
//            delay(150)
//            enterFullsScreen()
//        }
    }

    //修改了字体 或者大小
    override fun onChanged(index: Int) {
        GlobalScope.launch {
            ConfigUtil.getConfig().textSize = index
//            measurePageText.textSize = ConfigUtil.getTextSize().toFloat()
//            measurePageText.typeface = ConfigUtil.getTextSize().toFloat()
            measurePageText.setTextSize(ConfigUtil.getTextSize())
            MeasurePage.initView(measurePageText)
//            MeasurePage.getTextPaint().apply {
//                textSize = ConfigUtil.getTextSize().toFloat()
//                if (ConfigUtil.getTextStyle() != 0)
//                    typeface = ResourcesCompat.getFont(this@ReadActivity, ConfigUtil.getTextStyle())
//            }
            ConfigUtil.update()
            refreshLayout()
        }
    }

    @SuppressLint("WrongConstant")
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


    private fun nextPage() {
        var index = contentManager.findFirstVisibleItemPosition() + 1
        if (index >= contentAdapter.itemCount)
            index--
        contentManager.scrollToPosition(index)
//        contentManager.smoothScrollToPosition(readList,RecyclerView.State(),index)
    }

    private fun prePage() {
        var index = contentManager.findFirstVisibleItemPosition() - 1
        if (index < 0)
            index++
        contentManager.scrollToPosition(index)
//        contentManager.smoothScrollToPosition(readList,RecyclerView.State(),index)
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
                prePage()

                if (turnPageScroll.isSelected) {
                    if (!readList.canScrollVertically(-1))
                        preChapter()
                } else {
                    if (!readList.canScrollHorizontally(-1)) {
                        preChapter()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (autoScrollRunning) {
                    ConfigUtil.getConfig().autoScrollV = PageScrollController.reduceV()
                    ConfigUtil.update()
                    return true
                }
                nextPage()
                if (turnPageScroll.isSelected) {
                    if (!readList.canScrollVertically(1))
                        nextChapter()
                } else {
                    if (!readList.canScrollHorizontally(1)) {
                        nextChapter()
                    }
                }
                return true
            }
            //vivo 手机特殊按键
            308 -> {
                if (contentViewModel.getList().size == 0) return true
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
                if (contentViewModel.getList().size == 0) return
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
                if (contentViewModel.getList().size == 0) return
                pageOnClick()
                val position = contentManager.findFirstVisibleItemPosition()
                if (position == contentAdapter.itemCount - 1) {
                    nextChapter()
                } else {
                    contentManager.scrollToPositionWithOffset(position, -readList.height / 2)
//                    readList.scrollToPosition(position + 1)
                }
            }
            R.id.turnPageCover -> {
                if (turnPageCover.isSelected || contentViewModel.getList().size == 0) return
                turnPageScroll.isSelected = false
                turnPageCover.isSelected = true
                //让自动阅读按钮失效
                findViewById<TextView>(R.id.readAutoScroll).isEnabled = false
                ConfigUtil.getConfig().scrollDirection = LinearLayout.HORIZONTAL
                ConfigUtil.update()
                refreshLayout()
            }
            R.id.turnPageScroll -> {
                if (turnPageScroll.isSelected || contentViewModel.getList().size == 0) return
                turnPageCover.isSelected = false
                turnPageScroll.isSelected = true
                findViewById<TextView>(R.id.readAutoScroll).isEnabled = true
                ConfigUtil.getConfig().scrollDirection = LinearLayout.VERTICAL
                ConfigUtil.update()
                refreshLayout()
            }
        }
    }


    //保存阅读记录
    private fun saveReadRecord() {
        readOffset = 0
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

    //刷新布局 应用设置
    private fun refreshLayout() {
        if (contentViewModel.getList().size == 0) return
        //取消加载
        contentViewModel.cancelPrepare()
        //同时撤销回调 避免数据冲突
        contentViewModel.detachView()
        GlobalScope.launch {
            synchronized(contentViewModel.getList()) {
                val list = contentViewModel.getList()
                if (ConfigUtil.getDirection() == LinearLayout.HORIZONTAL) {
                    pagerSnapHelper.attachToRecyclerView(readList)
                    if (currentIndex >= list.size || currentIndex < 0)
                        currentIndex = 0
                    val item = list[currentIndex]
                    val listSize = list.size
                    GlobalScope.launch(Dispatchers.Main) {
                        list.clear()
                        contentAdapter.notifyItemRangeRemoved(0, listSize)
                    }
                    //重新绘制章节页面,并记录阅读位置
                    readOffset = contentViewModel.reMeasure(
                        currentReadID,
                        item.index,
                        item.totalPage
                    )
                    currentIndex = readOffset
                } else {
                    pagerSnapHelper.attachToRecyclerView(null)
                }

                GlobalScope.launch(Dispatchers.Main) {
                    contentViewModel.attachView(this@ReadActivity)
                    //重置适配器
                    contentAdapter = ReadListAdapter(contentViewModel.getList())
                    contentManager =
                        CenterLayoutManager(this@ReadActivity, ConfigUtil.getDirection(), false)
                    readList.adapter = contentAdapter
                    readList.layoutManager = contentManager
                    //加载完毕后，滑动的记录位置
                    if (currentIndex > 0) {
                        contentManager.scrollToPosition(currentIndex)
                    }
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
        }
    }

    override fun change(level: Int) {
        checkBattery(level)
    }

}