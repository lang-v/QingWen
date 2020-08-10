package com.novel.qingwen.net

import android.util.JsonReader
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.novel.qingwen.bean.BookContents
import com.novel.qingwen.bean.BookInfo
import com.novel.qingwen.bean.ChapterContent
import com.novel.qingwen.bean.SearchResult
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.net.service.Novel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetUtil {
    var searchCallback: ResponseCallback<SearchResult>? = null
    var infoCallback: ResponseCallback<BookInfo>? = null
    var contentsCallback: ResponseCallback<BookContents>? = null
    var chapterContentCallback: ResponseCallback<ChapterContent>? = null

    //搜索
    private val search: Retrofit = Retrofit.Builder()
        .baseUrl("https://souxs.leeyegy.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //书籍信息和目录
    private val infoAndContents = Retrofit.Builder()
        .baseUrl("https://infosxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //章节内容
    private val chapterContent = Retrofit.Builder()
        .baseUrl("https://contentxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    fun setInfo(callback: ResponseCallback<BookInfo>) {
        infoCallback = callback
    }

    fun setSearch(callback: ResponseCallback<SearchResult>) {
        searchCallback = callback
    }

    fun setContents(callback: ResponseCallback<BookContents>) {
        contentsCallback = callback
    }

    fun clear(callback: ResponseCallback<*>) {
        when (callback) {
            infoCallback -> infoCallback = null
            searchCallback -> searchCallback = null
            contentsCallback -> contentsCallback = null
            chapterContentCallback -> chapterContentCallback = null
        }
    }

    /**
     * 搜索
     */
    private var currentPage: Long = 0
    private var searchText = ""
    fun search(key: String, page: Long = 0) {
        if (searchText != key) searchText = key
        val request = search.create(Novel::class.java)
        val call: Call<SearchResult> = request.search(key, page)
        call.enqueue(object : Callback<SearchResult> {
            override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                searchCallback?.onFailure()
            }

            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                if (response.body() != null)
                    searchCallback?.onSuccess(response.body()!!)
                else
                    searchCallback?.onFailure()
            }
        })
    }

    fun searchNext() {
        search(searchText, ++currentPage)
    }

    /**
     * 获取小说详情
     */
    fun getBookInfo(id: Long) {
        val request = infoAndContents.create(Novel::class.java)
        val call: Call<BookInfo> = request.getBookInfo(id)
        call.enqueue(object : Callback<BookInfo> {
            override fun onFailure(call: Call<BookInfo>, t: Throwable) {
                infoCallback?.onFailure()
            }

            override fun onResponse(call: Call<BookInfo>, response: Response<BookInfo>) {
                if (response.body() == null)
                    infoCallback?.onFailure()
                else
                    infoCallback?.onSuccess(response.body()!!)
            }
        })
    }

    /**
     * 获取小说目录
     */
    fun getContents(id: Long) {
        infoAndContents
        val request = infoAndContents.create(Novel::class.java)
        val call: Call<ResponseBody> = request.getContents(id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                contentsCallback?.onFailure()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null) {
                    try {
                        val str = response.body()!!.string()
                        val json: String = str
                            .replace(",]", "]").replace(",}", "}")
                        contentsCallback?.onSuccess(Gson().fromJson(json, BookContents::class.java))
                    } catch (e: JsonSyntaxException) {
                        contentsCallback?.onFailure()
                    }
                } else
                    contentsCallback?.onFailure()
            }
        })
    }

    /**
     * 章节内容
     */
    fun getChapterContent(novelId: Long, chapterId: Long) {
        val request = chapterContent.create(Novel::class.java)
        val call: Call<ChapterContent> = request.getChapterContent(novelId, chapterId)
        call.enqueue(object : Callback<ChapterContent> {
            override fun onFailure(call: Call<ChapterContent>, t: Throwable) {
                chapterContentCallback?.onFailure()
            }

            override fun onResponse(
                call: Call<ChapterContent>,
                response: Response<ChapterContent>
            ) {
                if (response.body() == null)
                    chapterContentCallback?.onFailure()
                else
                    chapterContentCallback?.onSuccess(response.body()!!)
            }
        })
    }
}