package com.novel.qingwen.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.*
import com.novel.qingwen.view.adapter.BookShelfListAdapter
import com.novel.qingwen.view.adapter.FragmentAdapter
import com.novel.qingwen.view.fragment.BookStore
import com.novel.qingwen.view.fragment.MinePage
import com.novel.qingwen.view.fragment.SearchBook
import com.novel.qingwen.viewmodel.BookShelfVM
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_book_shelf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sl.view.elasticviewlibrary.ElasticLayout
import sl.view.elasticviewlibrary.base.BaseHeader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener, IBaseView,
    ElasticLayout.OnEventListener {
    private val viewModel: BookShelfVM by viewModels()
    private lateinit var adapter: BookShelfListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#669900")
        setContentView(R.layout.activity_main)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            RxPermissions(this)
                .requestEach(
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe {
                    if (it.name == Manifest.permission.WRITE_EXTERNAL_STORAGE && !it.granted) {
                        showError("没有存储权限App无法正常运行")
                        finish()
                    }
                }
        }
        init()
    }

    private fun init() {
        val list = ArrayList<Fragment>()
        list.add(SearchBook())
        list.add(BookStore())
        list.add(MinePage())
        val fragmentAdapter = FragmentAdapter(this, list)
        viewPager.adapter = fragmentAdapter
        fragmentAdapter.notifyDataSetChanged()
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        if (mainBookShelfMore.isActivated) {
                            mainBookShelfMore.isActivated = false
                            mainBookShelfMore.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.textColorPrimary
                                )
                            )
                        }
                        if (mainMine.isActivated) {
                            mainMine.isActivated = false
                            mainMine.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.textColorPrimary
                                )
                            )
                        }
                        mainSearchPageBtn.isActivated = true
                        mainSearchPageBtn.setTextColor(Color.parseColor("#669900"))
                    }
                    1 -> {
                        if (mainSearchPageBtn.isActivated) {
                            mainSearchPageBtn.isActivated = false
                            mainSearchPageBtn.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.textColorPrimary
                                )
                            )
                        }
                        if (mainMine.isActivated) {
                            mainMine.isActivated = false
                            mainMine.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.textColorPrimary
                                )
                            )
                        }
                        mainBookShelfMore.isActivated = true
                        mainBookShelfMore.setTextColor(Color.parseColor("#669900"))
                    }
                    2 -> {
                        if (mainSearchPageBtn.isActivated) {
                            mainSearchPageBtn.isActivated = false
                            mainSearchPageBtn.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.textColorPrimary
                                )
                            )
                        }
                        if (mainBookShelfMore.isActivated) {
                            mainBookShelfMore.isActivated = false
                            mainBookShelfMore.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.textColorPrimary
                                )
                            )
                        }
                        mainMine.isActivated = true
                        mainMine.setTextColor(Color.parseColor("#669900"))
                    }
                }
            }
        })
        viewPager.currentItem = 0

        //底部书架的初始化
        bottomSheetBehavior = BottomSheetBehavior.from(bottomBookShelf)
        var tabLayoutHeight = 0

        //加入消息队列 展开底部书架并获取高度
        tabLayout.post {
            tabLayoutHeight = tabLayout.measuredHeight
//            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                //随着bottomsheet的滑动改变tabtitle的大小和透明度
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bookShelfTab.alpha = 0f
                        bookShelfTab.scaleX = 0f
                        bookShelfTab.scaleY = 0f
                        bookShelfName.alpha = 1f
                        bookShelfBackground.alpha = 0f
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bookShelfTab.alpha = 1f
                        bookShelfTab.scaleX = 1f
                        bookShelfTab.scaleY = 1f
                        bookShelfName.alpha = 0f
                        bookShelfBackground.alpha = 1f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0) return
                bookShelfTab.alpha = 1 - slideOffset
                bookShelfTab.scaleX = 1 - slideOffset
                bookShelfTab.scaleY = 1 - slideOffset
                bookShelfBackground.alpha = 1 - slideOffset
                bookShelfName.alpha = slideOffset
//                Log.e("MainActivity","offset = $slideOffset height=$tabLayoutHeight offset = ${(tabLayoutHeight * slideOffset).toInt()}")
                val layoutParams = tabLayout.layoutParams as RelativeLayout.LayoutParams
                layoutParams.apply {
                    height = (tabLayoutHeight * (1 - slideOffset)).toInt()
//                    bottomMargin = -(tabLayoutHeight * slideOffset).toInt()
                }
                tabLayout.layoutParams = layoutParams
            }
        })
        //加载更多
        mainBookShelfMore.setOnClickListener(this)
        mainSearchPageBtn.setOnClickListener(this)
        mainMine.setOnClickListener(this)

        //确认是打开书架还是开始加载
        var scrollTarget = 0
        //设置刷新加载事件监听
        bookShelfRefresh.setOnElasticViewEventListener(this)
        //关闭递增阻尼，list滑动不会感受阻力
        bookShelfRefresh.setDamping(0.4f, false)
        bookShelfRefresh.setHeaderAdapter(object : BaseHeader(this, 200) {
            override fun scrollProgress(progress: Int) {
                super.scrollProgress(progress)
                scrollTarget =
                    if (progress < offset) 0 else if (progress in offset until offset + 200) 1 else 2
            }

            override fun releaseToDo() {
                super.releaseToDo()
                if (scrollTarget == 1) text.text = "继续下拉关闭书架,或者释放刷新"
                else if (scrollTarget == 2) text.text = "释放关闭书架"
            }

            override fun onRelease() {
                super.onRelease()
                if (scrollTarget == 2) bookShelfRefresh.cancelLoading(150L)
            }

            override fun onCancel() {
                super.onCancel()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
        bookShelfRefresh.setAnimTime(500L)
        adapter = BookShelfListAdapter(viewModel.getList())
        bookShelfList.adapter = adapter
        bookShelfList.layoutManager = LinearLayoutManager(this)
        bookShelfList.itemAnimator = DefaultItemAnimator()
        //分割线
        bookShelfList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        viewModel.attachView(this)
        //自动刷新
//        GlobalScope.launch (Dispatchers.Main){
//            delay(1000)
//            bookShelfRefresh.isRefreshing = true
//        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
        //自动刷新，但是不调用下拉框架
        viewModel.refresh()
        //刷新书架
        bookInfoUpdate()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    private fun bookInfoUpdate() {
//        adapter.refresh()
        val stringBuilder = StringBuilder()
        for (bookInfo in viewModel.getList()) {
            if (bookInfo.update) {
                if (stringBuilder.toString() != "")
                    stringBuilder.append("、")
                stringBuilder.append(bookInfo.novelName)
            }
        }
        if (stringBuilder.toString() == "") {
            bookShelfUpdateTip.text = ""
            bookShelfUpdateBookName.text = "暂无更新"
            bookShelfRefreshTime.text = ""
            bookShelfRefreshTime.visibility = View.INVISIBLE
        } else {
            bookShelfUpdateTip.text = "今日更新:"
            bookShelfUpdateBookName.text = stringBuilder.toString()
            bookShelfRefreshTime.text = SimpleDateFormat("HH:mm").format(Date())
            bookShelfRefreshTime.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mainBookShelfMore -> {
                viewPager.setCurrentItem(1, false)
            }

            R.id.mainSearchPageBtn -> {
                viewPager.setCurrentItem(0, false)
//                viewPager.currentItem = 0
            }

            R.id.mainMine -> {
                viewPager.setCurrentItem(2, false)
//                viewPager.currentItem = 2
            }
        }
    }

    private var lastTime = 0L
    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime <= 1500) {
            finish()
            return
        }
        lastTime = currentTime
        show("再按一次退出")
    }

    override fun showMsg(msg: String) {
        show(msg)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onComplete(target: Int, target2: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            adapter.refresh()
            if (bookShelfRefresh.isRefreshing) {
                delay(1000)
                bookShelfRefresh.isRefreshing = false
                //refreshHeader.overDo("完成")
                bookInfoUpdate()
            }
        }
    }

    override fun onLoad() {

    }

    var time = 0L
    override fun onRefresh() {
        if (System.currentTimeMillis() - time > 500L) {
            viewModel.refresh()
        }
        time = System.currentTimeMillis()
    }
}

