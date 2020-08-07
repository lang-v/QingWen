package com.novel.qingwen.net

import com.novel.qingwen.bean.BookInfo
import com.novel.qingwen.bean.SearchResult
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.net.service.Novel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetUtil {
    lateinit var searchCallback :ResponseCallback<SearchResult>
    lateinit var infoCallback: ResponseCallback<BookInfo>

    private val search: Retrofit = Retrofit.Builder()
        .baseUrl("https://souxs.leeyegy.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val info=Retrofit.Builder()
        .baseUrl("https://infosxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun setInfo(callback: ResponseCallback<BookInfo>){
        infoCallback = callback
    }

    fun setSearch(callback :ResponseCallback<SearchResult>){
        searchCallback = callback
    }


    /**
     * 搜索
     */
    private var currentPage = 0
    private var searchText = ""
    fun search(key: String,page:Int=0) {
        if (searchText != key) searchText = key
        val request = search.create(Novel::class.java)
        val call: Call<SearchResult> = request.search(key, page)
        call.enqueue(object : Callback<SearchResult> {
            override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                searchCallback.onFailure()
            }

            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                if (response.body() != null)
                    searchCallback.onSuccess(response.body()!!)
                else
                    searchCallback.onFailure()
            }
        })
    }
    fun searchNext(){
        search(searchText, ++currentPage)
    }

    /**
     * 获取小说详情
     */
    fun getBookInfo(id:Long){
        val request = info.create(Novel::class.java)
        val call:Call<BookInfo> = request.getBookInfo(id)
        call.enqueue(object :Callback<BookInfo>{
            override fun onFailure(call: Call<BookInfo>, t: Throwable) {
                infoCallback.onFailure()
            }

            override fun onResponse(call: Call<BookInfo>, response: Response<BookInfo>) {
                if (response.body() == null)
                    infoCallback.onFailure()
                else
                    infoCallback.onSuccess(response.body()!!)
            }
        })
    }
}