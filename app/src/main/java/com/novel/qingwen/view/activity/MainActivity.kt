package com.novel.qingwen.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.webkit.DownloadListener
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.databinding.ActivityMainBinding
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.adapter.BookShelfListAdapter
import com.novel.qingwen.view.adapter.FragmentAdapter
import com.novel.qingwen.view.fragment.MoreContent
import com.novel.qingwen.view.fragment.SearchBook
import com.novel.qingwen.viewmodel.BookShelfVM
import com.novel.qingwen.viewmodel.MainVM
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_book_shelf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sl.view.elasticviewlibrary.ElasticLayout
import sl.view.elasticviewlibrary.base.BaseHeader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener,IBaseView, ElasticLayout.OnEventListener {
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

//        RxPermissions(this)
//            .request(
//                Manifest.permission.ACCESS_NETWORK_STATE,
//                Manifest.permission.ACCESS_WIFI_STATE,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            .subscribe {
//                if (!it){
//                    showError("没有此权限App无法正常运行")
//                    finish()
//                }
//            }

        //启动页
        val binding:ActivityMainBinding = DataBindingUtil.setContentView(
            this@MainActivity,
            R.layout.activity_main
        )
        window.statusBarColor = Color.WHITE
        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            welcomePage.visibility = View.GONE
        }
        //initBugly()
        val list = ArrayList<Fragment>()
//        list.add(BookShelf())
        list.add(SearchBook())
        list.add(MoreContent())
        val adapter = FragmentAdapter(this, list)
        window.statusBarColor = Color.parseColor("#669900")
        binding.lifecycleOwner = this@MainActivity
        binding.mainVM = mainVM
        viewPager.adapter = adapter
        adapter.notifyDataSetChanged()
        init()
    }

//    private fun initBugly(){
//        Bugly.init(applicationContext, "20fec18d0c", false)
//        Beta.autoCheckUpgrade = true
//        Beta.storageDir = cacheDir
//        Beta.autoInstallApk = true
//        Beta.downloadListener = object :com.tencent.bugly.beta.download.DownloadListener{
//            override fun onReceive(p0: DownloadTask?) {
//
//            }
//
//            override fun onCompleted(p0: DownloadTask?) {
//
//            }
//
//            override fun onFailed(p0: DownloadTask?, p1: Int, p2: String?) {
//
//            }
//        }
//        Beta.upgradeStateListener = object :UpgradeStateListener{
//            override fun onUpgradeFailed(p0: Boolean) {
//
//            }
//
//            override fun onUpgradeSuccess(p0: Boolean) {
//
//            }
//
//            override fun onUpgradeNoVersion(p0: Boolean) {
//
//            }
//
//            override fun onUpgrading(p0: Boolean) {
//
//            }
//
//            override fun onDownloadCompleted(p0: Boolean) {
//                Toast.makeText(this@MainActivity,"下载${if (p0) "成功" else "失败"}",Toast.LENGTH_SHORT).show()
//            }
//
//        }
//        //Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
////        Beta.autoCheckUpgrade = true
////        Beta.autoInit = true
////        Beta.autoInstallApk = true
////        Beta.smallIconId = R.mipmap.ic_launcher
////        Beta.largeIconId = R.mipmap.ic_launcher
////        Beta.enableNotification = true//显示系统通知
////        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
////        Beta.registerDownloadListener(object :DownloadListener{
////            override fun onReceive(p0: DownloadTask?) {}
////
////            override fun onCompleted(p0: DownloadTask?) {
////                if (p0 != null)
////                    Beta.installApk(p0.saveFile)
////                Beta.unregisterDownloadListener()
////            }
////
////            override fun onFailed(p0: DownloadTask?, p1: Int, p2: String?) {
////                Beta.unregisterDownloadListener()
////            }
////
////        })
//        //只在主页显示更新弹窗
//        Beta.canShowUpgradeActs.add(MainActivity::class.java)
//    }

    fun haveInstallPermission():Boolean{
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.packageManager.canRequestPackageInstalls()
        } else {
            return true
        }
    }

    /**
     * 开启设置安装未知来源应用权限界面
     */
    fun startInstallPermissionSettingActivity(context: Context) {
        val intent = Intent()
        //获取当前apk包URI，并设置到intent中（这一步设置，可让“未知应用权限设置界面”只显示当前应用的设置项）
        val packageURI: Uri = Uri.parse("package:" + context.packageName)
        intent.data = packageURI
        //设置不同版本跳转未知应用的动作
        if (Build.VERSION.SDK_INT >= 26) {
            //intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
            intent.action = Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
        } else {
            intent.action = Settings.ACTION_SECURITY_SETTINGS
        }
        (context as Activity).startActivityForResult(
            intent,
            100
        )
        show("请开启未知应用安装权限")
    }


    private fun init(){
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        if (mainBookShelfMore.isActivated) {
                            mainBookShelfMore.isActivated = false
                            mainBookShelfMore.setTextColor(Color.BLACK)
                        }
                        mainSearchPageBtn.isActivated = true
                        mainSearchPageBtn.setTextColor(Color.parseColor("#669900"))
                    }
                    1 -> {
                        if (mainSearchPageBtn.isActivated) {
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
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bookShelfTab.alpha = 0f
                        bookShelfTab.scaleX = 0f
                        bookShelfTab.scaleY = 0f
                        bookShelfName.alpha = 1f
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bookShelfTab.alpha = 1f
                        bookShelfTab.scaleX = 1f
                        bookShelfTab.scaleY = 1f
                        bookShelfName.alpha = 0f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0) return
                bookShelfTab.alpha = 1 - slideOffset
                bookShelfTab.scaleX = 1 - slideOffset
                bookShelfTab.scaleY = 1 - slideOffset
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
        bookShelfRefresh.setAnimTime(300L)
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
        GlobalScope.launch (Dispatchers.Main){
            delay(1000)
            bookShelfRefresh.isRefreshing = true
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
        //刷新
//        adapter.refresh()
        bookInfoUpdate()
//        bookShelfRefresh.headerRefresh()
//        bookShelfRefresh.isRefreshing = true
        //viewModel.refresh()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    private fun bookInfoUpdate(){
        adapter.refresh()
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
            bookShelfRefreshTime.visibility = View.INVISIBLE
        }
        else{
            bookShelfUpdateTip.text = "今日更新:"
            bookShelfUpdateBookName.text = stringBuilder.toString()
            bookShelfRefreshTime.text = SimpleDateFormat("HH:mm").format(Date())
            bookShelfRefreshTime.visibility = View.VISIBLE
        }
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.mainBookShelfMore -> {
                viewPager.currentItem = 1
            }

            R.id.mainSearchPageBtn -> {
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
        GlobalScope.launch(Dispatchers.Main){
            delay(1000)
            adapter.refresh()
            if(bookShelfRefresh.isRefreshing){
                bookShelfRefresh.isRefreshing = false
                //refreshHeader.overDo("完成")
                bookInfoUpdate()
            }
        }
    }

    override fun onLoad() {

    }

    override fun onRefresh() {
        viewModel.refresh()
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode == RESULT_OK){
//            if (requestCode == 100){
//                Beta.installApk(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
}
infix fun Activity.showError(msg: String){
    Show.show(this, msg, Show.ERROR)
}
infix fun Activity.showSuccess(msg: String){
    Show.show(this, msg, Show.RIGHT)
}
infix fun Activity.show(msg: String){
    Show.show(this, msg, Show.NONE)
}
