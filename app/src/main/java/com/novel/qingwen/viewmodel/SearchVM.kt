package com.novel.qingwen.viewmodel

import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.bean.SearchResult
import com.novel.qingwen.bean.SearchResultItem
import com.novel.qingwen.net.NetUtil
import com.novel.qingwen.net.callback.ResponseCallback

class SearchVM : BaseVM(), ResponseCallback<SearchResult> {
    var searchText: MutableLiveData<String> = MutableLiveData()
    private val list = ArrayList<SearchResultItem>()
    //var adapter: SearchBookListAdapter = SearchBookListAdapter(list)
//    private val clickListener = View.OnClickListener {
//        doSearch()
//    }

    fun getList():ArrayList<SearchResultItem>{
        return list
    }

    init {
        NetUtil.setSearch(this)
    }

    fun doSearch() {
        if (searchText.value == "")
            return
        //清空列表重新搜索
//        list.clear()
//        adapter.notifyDataSetChanged()
        NetUtil.search(searchText.value!!)
    }

//    val onEditorActionListener: TextView.OnEditorActionListener =
//        TextView.OnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                doSearch()
//            }
//            false
//        }

//    /**
//     * BindAdapter方法必须是静态方法
//     */
//    companion object {
//        @JvmStatic
//        @BindingAdapter(
//            "app:bind_action_listener",
//            requireAll = false
//        )
//        fun setActionListener(
//            view: EditText,
//            onEditorActionListener: TextView.OnEditorActionListener
//        ) {
//            view.setOnEditorActionListener(onEditorActionListener)
//        }
//
//        @JvmStatic
//        @BindingAdapter(
//            "app:adapter",
//            requireAll = false
//        )
//        fun setAdapterAndLayoutManager(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
//            view.adapter = adapter
//
//        }
//    }

    override fun onFailure() {
        iView?.showMsg("连接出错，请检查网络。")
    }

    override fun onSuccess(t: SearchResult) {
        Log.e("SL",t.toString())
        list.addAll(t.data)
        iView?.onComplete()
        //GlobalScope.launch(Dispatchers.Main) {
//        adapter.notifyDataSetChanged()
        //}
    }
}