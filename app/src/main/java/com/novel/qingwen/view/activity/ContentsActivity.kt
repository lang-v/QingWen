package com.novel.qingwen.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.view.adapter.BookContentsListAdapter
import com.novel.qingwen.view.adapter.ItemOnClickListener
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.viewmodel.ContentsVM
import com.novel.qingwen.viewmodel.ResumeVM
import kotlinx.android.synthetic.main.activity_contents.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContentsActivity : AppCompatActivity(), IBaseView, ItemOnClickListener {
    companion object {
        // newInstance
        fun start(context: Activity, id: Long, name: String, status: String) {
            val intent = Intent(context, ContentsActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            intent.putExtra("status", status)
            context.startActivity(intent)
        }
    }

    //小说id
    private val id: Long by lazy {
        intent.getLongExtra("id", 501574L)
    }

    //小说名
    private val name: String by lazy {
        intent.getStringExtra("name")
    }

    //小说状态，为了方便后面判断小说是否完结
    private val status: String by lazy { intent.getStringExtra("status") }

    //自定义  重写了smoothScrollToPosition方法 实现修改滑动时间
    private val manager = MyLinearLayoutManager(this)
    private lateinit var adapter: BookContentsListAdapter
    private val viewModel: ContentsVM by viewModels()

    //数据由viewModel保管 数据视图分离
    private val headList by lazy { viewModel.getHeadList() }

    /**记录当前悬浮头显示个是第几个，
     * 悬浮头集合
     * @see headList
     */
    private var currentHeadViewIndex: Int = 0

    //加载框
    private val dialog: NoticeDialog by lazy { NoticeDialog.build(this, "请稍候") }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contents)
        init()
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
        dialog.show()
        //设置状态栏颜色 主题色
        window.statusBarColor = Color.parseColor("#ff669900")
        setSupportActionBar(contentsToolbar)
        //设置显示返回阿按钮
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //设置返回按钮样式
        contentsToolbar.setNavigationIcon(R.drawable.back_btn_selector)
        supportActionBar?.title = name
        //列表适配器
        adapter = BookContentsListAdapter(viewModel.getList(), this)
        //分割线
        contentsList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        contentsList.layoutManager = manager
        contentsList.adapter = adapter
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
                        manager.findViewByPosition(headList[currentHeadViewIndex + 1].id.toInt())
                    if (view == null || dy >= headView.height * 2) {
                        //当用户快速滑动时，会导致recyclerView中的headView快速滑过，被，从而导致这里的view为null
                        //重新检测当前headView
                        if (manager.findFirstVisibleItemPosition() >= headList[currentHeadViewIndex + 1].id.toInt()) {
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
                            manager.findViewByPosition(headList[currentHeadViewIndex].id.toInt())
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
                        manager.findViewByPosition(headList[currentHeadViewIndex + 1].id.toInt())
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
        if (id != -1L) {
            viewModel.load(id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contents_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //右上角返回按钮
            android.R.id.home -> {
                finish()
            }
            //跳转至顶部
            R.id.toTop -> {
//                contentsList.scrollToPosition(0)
                contentsList.smoothScrollToPosition(0)
            }
            //底部
            R.id.toBottom -> {
                contentsList.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showMsg(msg: String) {
        showError(msg)
        if (dialog.isShowing) dialog.dismiss()
    }

    override fun onComplete(target: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
            if (dialog.isShowing) dialog.dismiss()
            //如果有卷标（头部悬浮）
            if (headList.size != 0) {
                headView.visibility = View.VISIBLE
                headView.text = headList[0].name
            }
        }
    }

    //设置最大滑动时间 smoothScroll
    class MyLinearLayoutManager(context: Context) :
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
        override fun smoothScrollToPosition(
            recyclerView: RecyclerView?,
            state: RecyclerView.State?,
            position: Int
        ) {
            val scroller = object : LinearSmoothScroller(recyclerView!!.context) {
//                override fun calculateTimeForScrolling(dx: Int): Int {
//                    Log.e("SL","dx=$dx")
//                    return super.calculateTimeForScrolling(if (dx > 500) 500 else dx)
//                }

                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                    return super.calculateSpeedPerPixel(displayMetrics) * calc(recyclerView!!.adapter!!.itemCount)
                }

                private fun calc(count: Int): Float {
                    return if (count >= 500)
                        0.1f
                    else
                        1 - (count / 500f)
                }

            }
            scroller.targetPosition = position
            startSmoothScroll(scroller)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ReadActivity.REQCODE) {
            if (data == null) return
            val currentReadId = data.getLongExtra("currentReadID", -1L)
            if (currentReadId == -1L)return
            AlertDialog.Builder(ContextThemeWrapper(this, R.style.CommonDialog))
                .setTitle("喜欢这本书吗？")
                .setMessage("加入书架吧！")
                .setPositiveButton(
                    "好的"
                ) { _, _ ->
                    BookShelfListUtil.currentBookInfo?.apply { lastReadId=currentReadId }?.let {
                        BookShelfListUtil.insert(
                            it
                        )
                    }
                    /*
                    val vm: ResumeVM by viewModels()
                    val info = vm.info
                        info.id,
                        info.img,
                        info.name,
                        info.status,
                        false,
                        if (currentReadId == -1L) info.firstChapterID else currentReadId,
                        info.name,
                        info.firstChapterID,
                        info.lastChapterID,
                        info.lastChapterTime,
                        info.lastChapterName
                    )
                    BookShelfListUtil.insert(item)*/
                }.setNegativeButton("算了") { _, _ -> }.show()
        }
    }

    //list item click 跳转只readactivity 附带返回值
    override fun onClick(item: ContentsVM.ContentsInfo) {
        var target = false
        var target2= false
        BookShelfListUtil.getList().forEach {
            if (it.novelId == id) {
                target2 = true
                return@forEach
            }
        }
        ReadActivity.start(this, id, item.id, name, status,target2,target)
    }
}