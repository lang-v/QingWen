package com.novel.qingwen.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.databinding.ActivityResumeBinding
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

//    val name = "青文"
    private val viewModel: ResumeVM by viewModels()
    private lateinit var dataBinding: ActivityResumeBinding
    private val dialog:NoticeDialog by lazy { NoticeDialog.build(this,"请稍候") }
    companion object {
        fun start(context: Context, id: Long, name: String) {
            val intent = Intent(context, ResumeActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            context.startActivity(intent)
        }
    }

    private fun getStatusHeight(): Int {
        return ceil(25 * resources.displayMetrics.density).roundToInt()
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
            if (dialog.isShowing)dialog.dismiss()
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
            val statusHeight = getStatusHeight()
            resumeToolbar.setPadding(0, statusHeight, 0, 0)
            resumeToolbar.titleMarginTop = statusHeight/2
//            resumeToolbar.layoutParams.apply { height += statusHeight / 2 }
//            resumeBg.layoutParams.apply {
//                height+=statusHeight
//            }
//            resumeBg.setPadding(0,-statusHeight,0,0)
            resumeContents.setOnClickListener {
                ContentsActivity.start(this,viewModel.info.id,viewModel.info.name,viewModel.info.status)
            }
        }
    }

    private fun setTranslucentStatus(){
        val options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = options
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    //生成右上角菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.resume_menu, menu)
        return true
    }

    //处理 toolbar点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //返回按钮
                finish()
            }
            R.id.addToBookShelf -> {
                showSuccess("加入书架")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (dialog.isShowing)
            dialog.dismiss()
    }

    override fun onComplete(target: Int) {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}

