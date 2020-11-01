package com.novel.qingwen.view.fragment

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.view.adapter.BookStoreCategoryAdapter
import com.novel.qingwen.view.adapter.BookStoreListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.viewmodel.BookStoreVM
import kotlinx.android.synthetic.main.fragment_book_store.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sl.view.elasticviewlibrary.ElasticLayout
import sl.view.elasticviewlibrary.base.BaseFooter
import sl.view.elasticviewlibrary.base.BaseHeader

class BookStore : Fragment(), IBaseView {
    /**
     * new 、hot、vote、over
     */
    private var selectedStatus = "new"
    private var selectedId = 1
    private val viewModel: BookStoreVM by viewModels()
    private lateinit var bookStoreAdapter: BookStoreListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_book_store, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        val temp = arrayListOf("玄幻", "武侠", "都市", "历史", "科幻", "网游", "女生", "同人")
        val categoryAdapter = BookStoreCategoryAdapter(
            R.layout.fragment_book_store_item,
            temp
        ) { position, item ->
            selectedId = if (position == 7) 66
            else position + 1
            //确保可以获取到item的width
            item.post {
                val animator = ValueAnimator.ofInt(lineOne.width, item.measuredWidth)
                animator.duration = 150L
                animator.addUpdateListener {
                    val layoutParams = lineOne.layoutParams
                    layoutParams.width = it.animatedValue as Int
                    lineOne.layoutParams = layoutParams
                }
                animator.start()
                lineOne.animate().translationX(item.x).setDuration(150L).start()
            }
            getContent()
        }
        val categoryManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoryList.adapter = categoryAdapter
        categoryList.layoutManager = categoryManager
        categoryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lineOne.x -= dx
            }
        })
        bookStoreRefresh1.setOnScrollListener(object : ElasticLayout.OnScrollListener {
            override fun onScrolled(dx: Int, dy: Int) {
                lineOne.x -= dx
            }

            override fun preOnScrolled(scrollX: Int, scrollY: Int, dx: Int, dy: Int): Boolean {
                return false
            }
        })
        categoryAdapter.notifyDataSetChanged()

        val tmp = arrayListOf("最新", "最热", "评分", "完结")
        val statusManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val statusAdapter = BookStoreCategoryAdapter(
            R.layout.fragment_book_store_sub_item,
            tmp
        ) { position, item ->
            selectedStatus = when (position) {
                0 -> "new"
                1 -> "hot"
                2 -> "vote"
                3 -> "over"
                else -> "hot"
            }
            item.post {
                val animator = ValueAnimator.ofInt(lineTwo.width, item.measuredWidth)
                animator.duration = 150L
                animator.addUpdateListener {
                    val layoutParams = lineTwo.layoutParams
                    layoutParams.width = it.animatedValue as Int
                    lineTwo.layoutParams = layoutParams
                }
                animator.start()
                lineTwo.animate().translationX(item.x).setDuration(150L).start()
            }
            getContent()
        }
        statusList.adapter = statusAdapter
        statusList.layoutManager = statusManager
        statusAdapter.notifyDataSetChanged()
        statusList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lineTwo.x -= dx
            }
        })
        bookStoreRefresh2.setOnScrollListener(object : ElasticLayout.OnScrollListener {
            override fun onScrolled(dx: Int, dy: Int) {
                lineTwo.x -= dx
            }

            override fun preOnScrolled(scrollX: Int, scrollY: Int, dx: Int, dy: Int): Boolean {
                return false
            }
        })
        //设置弹回动画的执行时间
        bookStoreRefresh2.setAnimTime(500L)
        bookStoreRefresh1.setAnimTime(500L)

        val manager = LinearLayoutManager(context)
        bookStoreList.layoutManager = manager
        bookStoreAdapter = BookStoreListAdapter(viewModel.getList())
        bookStoreList.adapter = bookStoreAdapter
        bookStoreList.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        bookStoreAdapter.notifyDataSetChanged()
        bookStoreRefresh.setAnimTime(450L)
        bookStoreRefresh.setHeaderAdapter(object : BaseHeader(requireContext(), 150) {
            override fun onDo() {
                super.onDo()
                text.text = "请稍候..."
            }
        })
        bookStoreRefresh.setOnElasticViewEventListener(object : ElasticLayout.OnEventListener {
            override fun onLoad() {}
            override fun onRefresh() {
                viewModel.cancel()
                viewModel.getContent(selectedId, selectedStatus)
            }
        })
        bookStoreList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_SETTLING
                    && manager.findLastVisibleItemPosition() == viewModel.getList().size - 1)
                {
                    getNextContent()
                }
            }
        })
        getContent()
    }

    private fun getNextContent() {
        viewModel.getNextPage()
    }

    private fun getContent() {
        viewModel.getList().clear()
        bookStoreAdapter.notifyDataSetChanged()
        if (!bookStoreRefresh.isRefreshing)
            bookStoreRefresh.isRefreshing = true
    }

    override fun showMsg(msg: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(200L)
            if (bookStoreRefresh.isRefreshing)
                bookStoreRefresh.isRefreshing = false
        }
    }

    override fun onComplete(target: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200L)
            if (bookStoreRefresh.isRefreshing)
                bookStoreRefresh.isRefreshing = false
            bookStoreAdapter.notifyDataSetChanged()
        }
    }
}