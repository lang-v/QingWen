package com.novel.qingwen.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.novel.qingwen.R
import com.novel.qingwen.databinding.ActivityMainBinding
import com.novel.qingwen.view.adapter.FragmentAdapter
import com.novel.qingwen.view.fragment.SearchBook
import com.novel.qingwen.viewmodel.MainVM
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.fragment.BookShelf
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val mainVM:MainVM by viewModels()
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
        setContentView(R.layout.activity_welcome)
        window.statusBarColor = Color.WHITE
        initBugly()
//        GlobalScope.launch {
//            CrashReport.testJavaCrash()
//        }
        val list = ArrayList<Fragment>()
        list.add(BookShelf())
        list.add(SearchBook())
        val adapter = FragmentAdapter(this,list)

        GlobalScope.launch(Dispatchers.Main) {
            delay(600)//do something
            window.statusBarColor = Color.parseColor("#669900")
            val binding:ActivityMainBinding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)
            binding.lifecycleOwner = this@MainActivity
            binding.mainVM = mainVM
            viewPager.adapter = adapter
            adapter.notifyDataSetChanged()
            init()
        }
    }

    private fun initBugly(){
        Bugly.init(applicationContext, "20fec18d0c", false)
        //检查更新
//        Beta.checkUpgrade(true,true)
        //CrashReport.initCrashReport(applicationContext, "20fec18d0c", false)
//        val context: Context = applicationContext
//// 获取当前包名
//        val packageName: String = context.getPackageName()
//// 获取当前进程名
//        val processName = getProcessName(Process.myPid())
//// 设置是否为上报进程
//        val strategy = UserStrategy(context)
//        strategy.setUploadProcess(processName == null || processName == packageName)
//// 初始化Bugly
//        CrashReport.initCrashReport(context, "20fec18d0c", true, strategy)
    }

    private fun init(){
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0->{
                        if (mainSearchPageBtn.isActivated){
                            mainSearchPageBtn.isActivated = false
                            mainSearchPageBtn.setTextColor(Color.BLACK)
                        }
                        mainBookShelfPageBtn.isActivated = true
                        mainBookShelfPageBtn.setTextColor(Color.parseColor("#669900"))
                    }
                    1->{
                        if (mainBookShelfPageBtn.isActivated) {
                            mainBookShelfPageBtn.isActivated = false
                            mainBookShelfPageBtn.setTextColor(Color.BLACK)
                        }
                        mainSearchPageBtn.isActivated = true
                        mainSearchPageBtn.setTextColor(Color.parseColor("#669900"))
                    }
                }
            }
        })
        viewPager.currentItem = 0

        mainBookShelfPageBtn.setOnClickListener(this)
        mainSearchPageBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.mainBookShelfPageBtn->{
                viewPager.currentItem = 0
            }

            R.id.mainSearchPageBtn->{
                viewPager.currentItem = 1
            }
        }
    }

    private var lastTime = 0L
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime <= 1500) {
            finish()
            return
        }
        lastTime = currentTime
        show("再按一次退出")
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
