package com.novel.qingwen.viewmodel

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.SearchResult
import com.novel.qingwen.net.bean.SearchResultItem
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.net.callback.ResponseCallback

class SearchVM : BaseVM(), ResponseCallback<SearchResult> {
    var searchText: String = ""
    var visibility = Visibility()
    private val list = ArrayList<SearchResultItem>()
    class Visibility : BaseObservable() {
        @Bindable
        var visibility:Int = View.GONE
        set(value) {
            field = value
            notifyPropertyChanged(BR.searchVM)
        }
    }

    init {
        NetUtil.setSearch(this)
    }

    fun getList():ArrayList<SearchResultItem>{
        return list
    }

    fun isEmpty(str:String):Boolean{
        return str == ""
    }

    fun doSearch() {
        if (searchText == "") {
            return
        }
        //清空列表重新搜索
//        list.clear()
//        adapter.notifyDataSetChanged()
        NetUtil.search(searchText)
    }



    fun visibility(){
        visibility.visibility = if(searchText == " " || searchText == "")
            View.GONE
        else View.VISIBLE
        Log.e("SearchVM", searchText+"visiblity=$visibility")
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