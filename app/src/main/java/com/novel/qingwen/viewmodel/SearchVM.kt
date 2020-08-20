package com.novel.qingwen.viewmodel

import androidx.lifecycle.MutableLiveData
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.SearchResult
import com.novel.qingwen.net.bean.SearchResultItem
import com.novel.qingwen.utils.NetUtil
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
        if (searchText.value == "" || searchText.value==null) {
            return
        }
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
        list.addAll(t.data)
        iView?.onComplete()
        //GlobalScope.launch(Dispatchers.Main) {
//        adapter.notifyDataSetChanged()
        //}
    }
    override fun onCleared() {
        NetUtil.clear(this)
        super.onCleared()
    }
}