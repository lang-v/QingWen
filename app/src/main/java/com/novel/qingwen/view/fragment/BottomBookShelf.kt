package com.novel.qingwen.view.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.view.activity.show
import com.novel.qingwen.view.adapter.BookShelfListAdapter
import com.novel.qingwen.viewmodel.BookShelfVM
import kotlinx.android.synthetic.main.fragment_book_shelf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


open class BottomBookShelf : BottomSheetDialogFragment(), IBaseView {
    private val viewModel: BookShelfVM by viewModels()
    private lateinit var adapter: BookShelfListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
//        bookShelfRefresh.isRefreshing = true
        viewModel.refresh()
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    private fun init(){
        val dialog = dialog as BottomSheetDialog
//        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
//        dialog.dispatchTouchEvent()
        //把windowsd的默认背景颜色去掉，不然圆角显示不见
        dialog!!.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        //获取diglog的根部局
        val bottomSheet = dialog!!.delegate.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            //获取根部局的LayoutParams对象
            val layoutParams = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
            //layoutParams.height = getPeekHeight()
            //修改弹窗的最大高度，不允许上滑（默认可以上滑）
            bottomSheet.layoutParams = layoutParams
            val behavior = BottomSheetBehavior.from(bottomSheet)
            //peekHeight即弹窗的最大高度
            behavior.peekHeight = 140
            behavior.isHideable = false
            behavior.addBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    //折叠状态
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED){

                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
            // 初始为展开状态
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheet.setOnTouchListener{ p0, p1 ->

                false
            }
        }


//        bookShelfRefresh.setColorSchemeColors(Color.GREEN, Color.BLUE, Color.YELLOW)
        //刷新
//        bookShelfRefresh.setOnRefreshListener {
//            viewModel.refresh()
//        }
        adapter = BookShelfListAdapter(viewModel.getList())
        bookShelfList.adapter = adapter
        bookShelfList.layoutManager = LinearLayoutManager(context)
        //分割线
        bookShelfList.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_shelf, container, false)
    }

    override fun showMsg(msg: String) {
        activity?.show(msg)
    }

    override fun onComplete(target: Int) {
        GlobalScope.launch(Dispatchers.Main){
//            if (adapter.itemCount == 0){
//                bookShelfTips.visibility = View.VISIBLE
//            }else {
//                if (bookShelfTips.visibility != View.GONE){
//                    bookShelfTips.visibility = View.GONE
//                }
//            }
            adapter.notifyDataSetChanged()
//            if (bookShelfRefresh.isRefreshing)
//                bookShelfRefresh.isRefreshing = false
        }
    }
    private fun getPeekHeight(): Int {
        //设置弹窗高度为屏幕高度的3/4
        return resources.displayMetrics.heightPixels
    }
}