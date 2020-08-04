package com.novel.qingwen.net

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
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://souxs.leeyegy.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun setSearch(callback :ResponseCallback<SearchResult>){
        searchCallback = callback
    }

    var currentPage = 0
    var searchText = ""

    fun search(key: String,page:Int=0) {
        if (searchText != key) searchText = key
        val request = retrofit.create(Novel::class.java)
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
}