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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.broadcast.BatteryChangeListener
import com.novel.qingwen.broadcast.BatteryChangeReceiver
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.view.adapter.ItemOnClickListener
import com.novel.qingwen.view.adapter.ReadListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.view.widget.CustomLinearLayoutManager
import com.novel.qingwen.view.widget.CustomSeekBar
import com.novel.qingwen.viewmodel.ContentsVM
import com.novel.qingwen.viewmodel.ReadVM
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs


//todo 交互优化
class ReadActivity : AppCompatActivity(), IBaseView, CustomSeekBar.OnProgressChanged,
    ItemOnClickListener, View.OnClickListener, BatteryChangeListener {
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
    private var contentManager = CustomLinearLayoutManager(this)

    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }

    //显示电量
    private val broadcastReceiver = BatteryChangeReceiver()

    //记录是否在等待刷新布局
    private var waitForRefresh = false

    override fun onResume() {
        Log.e("life cycle", "onResume")

        registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        super.onResume()
    }

    override fun onPause() {
        Log.e("life cycle", "onPause")
        unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("life cycle", "onCreate")
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
            contentViewModel.getChapter(chapterId, false, 1)//前中后三章
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
        Log.e("life cycle", "onRestart")
        if (waitForRefresh) {
            refreshLayout()
            waitForRefresh = false
        }
        super.onRestart()
    }

    override fun onStart() {
        Log.e("life cycle", "onStart")
        super.onStart()
        contentViewModel.attachView(this)
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
        Log.e("life cycle", "onStop")
        super.onStop()
        contentViewModel.detachView()
        //设置返回值
        if (!isInBookShelf) return
        readOffset = readList.getChildAt(0).top
        //如果这本书是已经加入书架了的就更新阅读位置
        BookShelfListUtil.getList().forEach {
            if (it.novelId == novelId) {
                it.lastReadId = currentReadID
                it.lastReadOffset = readOffset
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
                finish()
            }
            R.id.readMenuContents -> {
                //showSuccess("打开目录")
//                ContentsActivity.start(this,novelId,novelName,status)
//                finish()
                ContentsActivity.start(this, novelId, novelName, status)
                finish()
//                if (readDrawerLayout.isDrawerOpen(readBottomView)) {
//                    readDrawerLayout.openDrawer(readBottomView)
//                    if (contentsViewModel.getList().size == 0)
//                        contentsViewModel.load(novelId)
//                }
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
//        readSetting.y += (readSetting.height).toFloat()
        //文字大小进度条设置监听
        readTextSizeSeekBar.setOnSeekBarChangedListener(this)
        //此view继承自TextView 在生成时的所有属性保存在ConfigUtil管理的Config中
        // 所以在xml中无法直接修改其属性，需要代码修改
        readHead.textSize = 12f
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
        //设置背景色
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
        readList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val item = contentViewModel.getList()[contentManager.findFirstVisibleItemPosition()]
                readHead.text = item.name
                currentReadID = item.chapterId
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //设置界面处于开启状态 不处理滑动加载事件
                if (isOpen) return
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
                    if (abs(event.x - oldX) > 5f
                        || abs(event.y - oldY) > 5f
                    ) {
                        target = false
                    }
                }
            }
            target
        }
        //contentsInit()
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
            showError("这是第一章哦")
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
            else
                showError("已经是最后一章了。")
            loadLock = false
            return
        }
        //加载下一章
        contentViewModel.getChapter(nid)
        dialog.show()
    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (loadLock)
            loadLock = false
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
                    contentAdapter.notifyItemInserted(contentAdapter.itemCount)
                    if (contentAdapter.itemCount == 1) {
//                        contentManager.scrollToPositionWithOffset(0, abs(readOffset))
                        readList.scrollBy(0, -readOffset)
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
        if (isOpen) {
            closeSetting()
            //允许滑动
//            readList.isEnabled = true
//            readList.isClickable = true
//            contentManager.setScrollEnabled(true)
        } else {
            openSetting()
            //禁止滑动
//            readList.isClickable = false
//            contentManager.setScrollEnabled(false)
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
        ObjectAnimator.ofFloat(readSetting, "y", readSetting.y, readSetting.y - readSetting.height - getNavigationBarHeight())
            .setDuration(300)
    }
    private val bottomClose: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(readSetting, "y", readSetting.y, readSetting.y + readSetting.height + getNavigationBarHeight())
            .setDuration(300)
    }

    //获取导航栏高度
    private fun getNavigationBarHeight():Int{
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
            topClose.cancel()
            bottomClose.cancel()
            settingLock.unlock()
        }
        settingLock.lock()
        isOpen = true

        topOpen.start()
        bottomOpen.start()
        // 非全屏显示，显示状态栏和导航栏
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
//        getWindow().getDecorView().setSystemUiVisibility(
//            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_VISIBLE
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        )
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    private fun closeSetting() {
        if (settingLock.isLocked) {
            topOpen.cancel()
            bottomOpen.cancel()
            settingLock.unlock()
        }
        settingLock.lock()
        isOpen = false
        topClose.start()
        bottomClose.start()
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

//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
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


    //目录页选中
    override fun onClick(item: ContentsVM.ContentsInfo) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isOpen) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                readList.scrollBy(0, -readList.height)
                if (!readList.canScrollVertically(-1)) {
                    preChapter()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                readList.scrollBy(0, readList.height)
                if (!readList.canScrollVertically(1)) {
                    nextChapter()
                }
                return true
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
            readList.adapter = contentAdapter
            readList.layoutManager = contentManager
            contentAdapter.notifyDataSetChanged()
            contentManager.scrollToPositionWithOffset(position, readOffset)
            //时钟
            readClock.setTextColor(ConfigUtil.getTextColor())
            //head
            readHead.setTextColor(ConfigUtil.getTextColor())
            //设置背景
            readLayout.setBackgroundColor(ConfigUtil.getBackgroundColor())
        }
    }

    override fun change(level: Int) {
        checkBattery(level)
    }

}