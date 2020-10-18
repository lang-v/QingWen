package com.novel.qingwen.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.novel.qingwen.net.bean.*
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.net.service.Novel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 请求过程使用try catch捕获Json解析异常
 */
object NetUtil {
    var searchCallback: ResponseCallback<SearchResult>? = null
    var infoCallback: ResponseCallback<BookInfo>? = null
    var contentsCallback: ResponseCallback<BookContents>? = null
    var chapterContentCallback: ResponseCallback<ChapterContent>? = null
    var categoryCallBack: ResponseCallback<BookStoreItem>? = null

    //搜索
    private val search: Retrofit = Retrofit.Builder()
        .baseUrl("https://souxs.leeyegy.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * https 改成 http 之后加载速度明显提升
     */
    //书籍信息和目录
    private val infoAndContents = Retrofit.Builder()
        .baseUrl("http://infosxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //章节内容
    private val chapterContent = Retrofit.Builder()
        .baseUrl("http://contentxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()


    //分类小说
    private val category = Retrofit.Builder()
        .baseUrl("https://scxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun setInfo(callback: ResponseCallback<BookInfo>) {
        infoCallback = callback
    }

    fun setSearch(callback: ResponseCallback<SearchResult>) {
        searchCallback = callback
    }

    fun setCategory(callback: ResponseCallback<BookStoreItem>) {
        categoryCallBack = callback
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
            categoryCallBack -> categoryCallBack = null
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
        search(
            searchText,
            ++currentPage
        )
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
                else {
                    try {
                        infoCallback?.onSuccess(response.body()!!)
                    } catch (e: JsonSyntaxException) {
                        infoCallback?.onFailure()
                    }
                }
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


    var chapterCall: Call<ChapterContent>? = null

    /**
     * 章节内容
     */
    fun getChapterContent(novelId: Long, chapterId: Long, slient: Boolean = false) {
        val request = chapterContent.create(Novel::class.java)
        chapterCall = request.getChapterContent(novelId, chapterId)
        chapterCall?.enqueue(object : Callback<ChapterContent> {
            override fun onFailure(call: Call<ChapterContent>, t: Throwable) {
                if (!slient)
                    chapterContentCallback?.onFailure()
            }

            override fun onResponse(
                call: Call<ChapterContent>,
                response: Response<ChapterContent>
            ) {
                if (response.body() != null) {
                    try {
                        chapterContentCallback?.onSuccess(response.body()!!)
                    } catch (e: JsonSyntaxException) {
                        if (!slient)
                            chapterContentCallback?.onFailure()
                    }
                } else {
                    if (!slient)
                        chapterContentCallback?.onFailure()
                }
            }
        })
    }

    //取消加载章节
    fun cancelLoadChapter() {
        chapterCall?.cancel()
    }



    private var categoryCall: Call<BookStoreItem>? = null
    fun cancelGetCategory(){
        categoryCall?.cancel()
    }
    /**
     * @param categoryId 分类ID
     * @param status 状态
     * @param page 页数
     */
    fun getCategory(categoryId:Int,status:String,page:Int){
        val request = category.create(Novel::class.java)
        categoryCall = request.getCategory(categoryId, status, page)
        categoryCall?.enqueue(object : Callback<BookStoreItem> {
            override fun onFailure(call: Call<BookStoreItem>, t: Throwable) {
                categoryCallBack?.onFailure()
            }

            override fun onResponse(call: Call<BookStoreItem>, response: Response<BookStoreItem>) {
                if (response.body() == null)
                    categoryCallBack?.onFailure()
                else {
                    try {
                        response.body()?.let {
                            categoryCallBack?.onSuccess(it)
                        }
                    } catch (e: JsonSyntaxException) {
                        categoryCallBack?.onFailure()
                    }
                }
            }

        })
    }
}