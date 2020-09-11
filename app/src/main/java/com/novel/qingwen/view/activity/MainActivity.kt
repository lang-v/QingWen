package com.novel.qingwen.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.databinding.ActivityMainBinding
import com.novel.qingwen.view.adapter.FragmentAdapter
import com.novel.qingwen.view.fragment.SearchBook
import com.novel.qingwen.viewmodel.MainVM
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.adapter.BookShelfListAdapter
import com.novel.qingwen.view.fragment.MoreContent
import com.novel.qingwen.viewmodel.BookShelfVM
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.Bugly
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_book_shelf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sl.view.elasticviewlibrary.ElasticView
import sl.view.elasticviewlibrary.base.BaseHeader
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener,IBaseView, ElasticView.OnEventListener {
    private val mainVM:MainVM by viewModels()
    private val viewModel: BookShelfVM by viewModels()
    private lateinit var adapter: BookShelfListAdapter
    private lateinit var bottomSheetBehavior:BottomSheetBehavior<*>
    private val refreshHeader :BaseHeader by lazy {
        BaseHeader(this, 200)
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RxPermissions(this)
            .request(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_PHONE_STATE
            )
            .subscribe {
                if (!it){
                    showError("没有此权限App无法正常运行")
                    finish()
                }
            }

        //启动页
        val binding:ActivityMainBinding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)
        window.statusBarColor = Color.WHITE
        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            welcomePage.visibility = View.GONE
        }
        initBugly()
        val list = ArrayList<Fragment>()
//        list.add(BookShelf())
        list.add(SearchBook())
        list.add(MoreContent())
        val adapter = FragmentAdapter(this,list)
        window.statusBarColor = Color.parseColor("#669900")
        binding.lifecycleOwner = this@MainActivity
        binding.mainVM = mainVM
        viewPager.adapter = adapter
        adapter.notifyDataSetChanged()
        init()
    }

    private fun initBugly(){
        Bugly.init(applicationContext, "20fec18d0c", false)
    }

    private fun init(){
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0->{
                        if (mainBookShelfMore.isActivated) {
                            mainBookShelfMore.isActivated = false
                            mainBookShelfMore.setTextColor(Color.BLACK)
                        }
                        mainSearchPageBtn.isActivated = true
                        mainSearchPageBtn.setTextColor(Color.parseColor("#669900"))
                    }
                    1->{
                        if (mainSearchPageBtn.isActivated){
                            mainSearchPageBtn.isActivated = false
                            mainSearchPageBtn.setTextColor(Color.BLACK)
                        }
                        mainBookShelfMore.isActivated = true
                        mainBookShelfMore.setTextColor(Color.parseColor("#669900"))
                    }

                }
            }
        })
        viewPager.currentItem = 0

//        val bottomSheetBehavior = BottomBookShelfBehavior<CardView>().fromCopy(bottomBookShelf)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomBookShelf)
        var tabLayoutHeight = 0
        tabLayout.post {
            tabLayoutHeight = tabLayout.measuredHeight
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheetBehavior.addBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_EXPANDED->{
                        bookShelfTab.alpha = 0f
                        bookShelfName.alpha = 1f
                    }

                    BottomSheetBehavior.STATE_COLLAPSED->{
                        bookShelfTab.alpha = 1f
                        bookShelfName.alpha = 0f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0)return
                bookShelfTab.alpha = 1-slideOffset
                bookShelfName.alpha = slideOffset
//                Log.e("MainActivity","offset = $slideOffset height=$tabLayoutHeight offset = ${(tabLayoutHeight * slideOffset).toInt()}")
                val layoutParams = tabLayout.layoutParams as RelativeLayout.LayoutParams
                layoutParams.apply {
                    height = (tabLayoutHeight *  (1-slideOffset)).toInt()
//                    bottomMargin = -(tabLayoutHeight * slideOffset).toInt()
                }

                tabLayout.layoutParams = layoutParams
            }
        })
//        BottomBookShelf().show(supportFragmentManager,"BookShelf")
        mainBookShelfMore.setOnClickListener(this)
        mainSearchPageBtn.setOnClickListener(this)
//        bookShelfRefresh.setColorSchemeColors(Color.GREEN,Color.BLUE,Color.YELLOW)
        //刷新
//        bookShelfRefresh.setOnRefreshListener {
//            viewModel.refresh()
//        }
        bookShelfRefresh.setOnElasticViewEventListener(this)
        bookShelfRefresh.setHeaderAdapter(refreshHeader)
        bookShelfRefresh.setAnimTime(200L)
        adapter = BookShelfListAdapter(viewModel.getList())
        bookShelfList.adapter = adapter
        bookShelfList.layoutManager = LinearLayoutManager(this)
        //分割线
        bookShelfList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    override fun onStart() {
        super.onStart()
        //刷新
        bookShelfRefresh.headerRefresh()
        viewModel.attachView(this)
//        bookShelfRefresh?.isRefreshing = true
        viewModel.refresh()
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.mainBookShelfMore->{
                viewPager.currentItem = 1
            }

            R.id.mainSearchPageBtn->{
                viewPager.currentItem = 0
            }
        }
    }

    private var lastTime = 0L
    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
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
    override fun onComplete(target: Int) {
        GlobalScope.launch (Dispatchers.Main){
            delay(500)
            if (adapter.itemCount == 0){
                bookShelfTips.visibility = View.VISIBLE
            }else {
                if (bookShelfTips.visibility != View.GONE){
                    bookShelfTips.visibility = View.GONE
                }
            }
            adapter.notifyDataSetChanged()
            if(refreshHeader.isDoing){
                refreshHeader.overDo("完成")
                val stringBuilder = StringBuilder()
                for (bookInfo in viewModel.getList()) {
                    if (bookInfo.update){
                        if (stringBuilder.toString() != "")
                            stringBuilder.append("、")
                        stringBuilder.append(bookInfo.novelName)
                    }
                }
                if (stringBuilder.toString() == "") {
                    bookShelfUpdateTip.text = ""
                    bookShelfUpdateBookName.text = "暂无更新"
                    bookShelfRefreshTime.text = ""
                }
                else{
                    bookShelfUpdateTip.text = "今日更新:"
                    bookShelfUpdateBookName.text = stringBuilder.toString()
                    bookShelfRefreshTime.text = SimpleDateFormat("HH:mm").format(Date())
                }
            }
//            if (bookShelfRefresh.isRefreshing)
//                bookShelfRefresh.isRefreshing = false
        }
    }


    override fun onLoad() {

    }

    override fun onRefresh() {
        viewModel.refresh()
    }
}
infix fun Activity.showError(msg:String){
    Show.show(this,msg,Show.ERROR)
}
infix fun Activity.showSuccess(msg:String){
    Show.show(this,msg,Show.RIGHT)
}
infix fun Activity.show(msg: String){
    Show.show(this,msg,Show.NONE)
}
