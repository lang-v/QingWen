package com.novel.qingwen.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.view.activity.show
import com.novel.qingwen.view.adapter.BookShelfListAdapter
import com.novel.qingwen.viewmodel.BookShelfVM
import kotlinx.android.synthetic.main.fragment_book_shelf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookShelf : Fragment(), IBaseView{
    private val viewModel:BookShelfVM by viewModels()
    private lateinit var adapter:BookShelfListAdapter

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
//        bookShelfRefresh.setColorSchemeColors(Color.GREEN,Color.BLUE,Color.YELLOW)
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
        GlobalScope.launch (Dispatchers.Main){
            if (adapter.itemCount == 0){
                bookShelfTips.visibility = View.VISIBLE
            }else {
                if (bookShelfTips.visibility != View.GONE){
                    bookShelfTips.visibility = View.GONE
                }
            }
            adapter.notifyDataSetChanged()
//            if (bookShelfRefresh.isRefreshing)
//                bookShelfRefresh.isRefreshing = false
        }
    }
}