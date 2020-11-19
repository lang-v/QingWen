package com.novel.qingwen.view.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.databinding.ActivityResumeBinding
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.viewmodel.ResumeVM
import kotlinx.android.synthetic.main.activity_resume.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class ResumeActivity : AppCompatActivity(), IBaseView {
    private val id by lazy {
        intent.getLongExtra("id", 265245)
    }
    private val name by lazy {
        intent.getStringExtra("name")
    }

    private val viewModel: ResumeVM by viewModels()
    private lateinit var dataBinding: ActivityResumeBinding
    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }

    companion object {
        fun start(context: Fragment, id: Long, name: String,view:View) {
            val intent = Intent(context.requireContext(), ResumeActivity::class.java)
            intent.putExtra("id", id)
            BookShelfListUtil.currentBookInfo
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                context.requireActivity(),
                view,
                "novelPic"
            ).toBundle()
            intent.putExtra("name", name)
            context.startActivity(intent,bundle)
        }
    }

    private fun getStatusHeight(): Int {
        var height = 0
        val resourceId =
            applicationContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            height = applicationContext.resources.getDimensionPixelSize(resourceId)
        }
        return height
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_resume)
        dataBinding.resumeVM = viewModel
        dataBinding.lifecycleOwner = this
        init()
        setTranslucentStatus()
        //Log.e("SL","id=${id}")
        if (id != -1L) {
            viewModel.load(id)
            if (dialog.isShowing) dialog.dismiss()
            dialog.show()
        } else
            showError("获取信息失败，未找到此书")
        //viewModel.name="123456"
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
        setSupportActionBar(resumeToolbar)
        if (supportActionBar != null) {
            //显示返回按钮
            resumeToolbar.setNavigationIcon(R.drawable.back_btn_selector)
            supportActionBar!!.title = name
            //动态设置toolbar高度
            val statusHeight = (getStatusHeight() * 0.9).roundToInt()
//            resumeToolbar.setPadding(0, statusHeight/2, 0, 0)
//            resumeToolbar.textAlignment = TextView.
//            resumeToolbar.titleMarginTop = (statusHeight*1.5).toInt()
            resumeToolbar.layoutParams.apply {
                height += statusHeight
            }
            resumeBg.layoutParams.apply {
                height += statusHeight
            }

            resumeToolbar.setPadding(0, statusHeight, 0, 0)
//            resumeBg.layoutParams.apply {
//                height+=statusHeight
//            }
//            resumeBg.setPadding(0,-statusHeight,0,0)
            resumeContents.setOnClickListener {
                ContentsActivity.start(
                    this,
                    viewModel.info.id,
                    viewModel.info.name,
                    viewModel.info.status
                )
            }
        }
    }

    //设置沉浸状态栏
    private fun setTranslucentStatus() {
        val options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = options
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    private lateinit var addToBookShelf :MenuItem
    //生成右上角菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.resume_menu, menu)
        addToBookShelf = menu?.findItem(R.id.addToBookShelf)!!
        BookShelfListUtil.getList().forEach {
            if (it.novelId == id){
//                addToBookShelf.isEnabled = false
                resumeToolbar.menu.setGroupVisible(0,false)
                return@forEach
            }
        }
        return true
    }

    //处理 toolbar点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //返回按钮
//                finish()
                onBackPressed()
            }
            R.id.addToBookShelf -> {
                if(!BookShelfListUtil.getList().contains(BookShelfListUtil.currentBookInfo)){
//                    Log.e("ResumeActivity","加入书架")
                    showSuccess("加入书架")
                    BookShelfListUtil.currentBookInfo?.let {
                        BookShelfListUtil.insert(it)
                    }
//                    BookShelfListUtil.refresh()
                    resumeToolbar.menu.setGroupVisible(0,false)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (dialog.isShowing)
            dialog.dismiss()
    }

    override fun onComplete(target: Int, target2: Int) {
        if (dialog.isShowing)
            dialog.dismiss()
//        if (BookShelfListUtil.getList().contains(BookShelfListUtil.currentBookInfo)){
//            Log.e("ResumeActivity","此书已在书架")
//            GlobalScope.launch(Dispatchers.Main) {
//                addToBookShelf.isEnabled = false
//            }
//        }
    }
}

