package com.novel.qingwen.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.databinding.FragmentSearchBookBinding
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.view.adapter.SearchBookListAdapter
import com.novel.qingwen.view.dialog.NoticeDialog
import com.novel.qingwen.viewmodel.SearchVM
import com.novel.qingwen.utils.Show
import com.novel.qingwen.view.activity.ResumeActivity
import kotlinx.android.synthetic.main.fragment_search_book.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchBook : Fragment(), IBaseView, TextView.OnEditorActionListener, View.OnClickListener {
    private val viewModel: SearchVM by viewModels()
    private lateinit var adapter: SearchBookListAdapter
    private val dialog: NoticeDialog by lazy {
        NoticeDialog.build(requireContext(), "搜索中...")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSearchBookBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search_book, container, false)
        binding.searchVM = viewModel
        binding.lifecycleOwner = this
        return binding.root
//        return inflater.inflate(R.fragment_search_list_item.fragment_search_book,container,false)
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        //viewModel = ViewModelProviders.of(this).get(SearchVM::class.java)
    }

    private fun init() {
        searchET.setOnEditorActionListener(this)
        searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s.isNullOrEmpty()
                closeBtn.visibility = if (s.isNullOrEmpty())
                    View.GONE
                else View.VISIBLE
            }
        })
        closeBtn.setOnClickListener(this)
        adapter = SearchBookListAdapter(viewModel.getList()){
                item,view->
        ResumeActivity.start(this,item.Id.toLong(),item.Name,view)
    }
        val manager = LinearLayoutManager(context)
        searchListView.adapter = adapter
        searchListView.layoutManager = manager
        searchListView.itemAnimator = DefaultItemAnimator()
        //默认分割线
        searchListView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        searchListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && manager.findLastVisibleItemPosition() == (adapter.itemCount - 1)
                    && (searchListView.canScrollVertically(-1) != searchListView.canScrollVertically(
                        1
                    ))
                ) {
                    NetUtil.searchNext()//下一页内容
                }
            }
        })
    }

    override fun showMsg(msg: String) {
        GlobalScope.launch(Dispatchers.Main) {
            searchListView.visibility =
                if (viewModel.getList().size == 0) View.GONE else View.VISIBLE
            tips.visibility = if (viewModel.getList().size == 0) View.VISIBLE else View.GONE
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (dialog.isShowing)
                dialog.dismiss()
        }
    }

    override fun onComplete(target: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            searchListView.visibility =
                if (viewModel.getList().size == 0) View.GONE else View.VISIBLE
            tips.visibility = if (viewModel.getList().size == 0) View.VISIBLE else View.GONE
            adapter.notifyDataSetChanged()
            if (dialog.isShowing)
                dialog.dismiss()
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH)
            doSearch()
        return false
    }

    override fun onClick(v: View?) {
        (searchET as TextView).text = ""
        viewModel.getList().clear()
        adapter.notifyDataSetChanged()
        closeBtn.visibility = View.GONE
        searchListView.visibility = View.GONE
        tips.visibility = View.VISIBLE
    }

    private fun doSearch() {
        if (searchET.text.toString() == "") {
            Show.show(requireContext(), "请输入后再尝试", Show.ERROR)
            searchET.isFocusable = true
            return
        }
        //清空内容
        viewModel.getList().clear()
        viewModel.doSearch()
        if (dialog.isShowing) dialog.dismiss()
        dialog.show()
    }
}