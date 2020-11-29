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
    var userDataCallback: ResponseCallback<LoginResult>? = null
    var avatarCallback: ResponseCallback<Avatar>? = null
    var bookShelfCallback: ResponseCallback<BookShelf>? = null
    var usernameCallback: ResponseCallback<BaseResponse>? = null

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
        .baseUrl("https://infosxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //章节内容
    private val chapterContent = Retrofit.Builder()
        .baseUrl("https://contentxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    //分类小说
    private val category = Retrofit.Builder()
        .baseUrl("https://scxs.pysmei.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

//        .baseUrl("http://localhost:8080/Gradle___com_novel_qingwen___QingWen_1_0_war/")

    private val userData = Retrofit.Builder()
        .baseUrl("http://39.97.127.33/qingwen/")
//        .baseUrl("http://127.0.0.1:8080/Gradle___com_novel_qingwen___QingWen_1_0_war/")
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

    fun setUserData(callback: ResponseCallback<LoginResult>) {
        userDataCallback = callback
    }

    fun setAvatar(callback: ResponseCallback<Avatar>) {
        avatarCallback = callback
    }

    fun setBookShelf(callback: ResponseCallback<BookShelf>) {
        bookShelfCallback = callback
    }

    fun setUserName(callback: ResponseCallback<BaseResponse>) {
        usernameCallback = callback
    }

    fun clear(callback: ResponseCallback<*>) {
        when (callback) {
            infoCallback -> infoCallback = null
            searchCallback -> searchCallback = null
            contentsCallback -> contentsCallback = null
            chapterContentCallback -> chapterContentCallback = null
            categoryCallBack -> categoryCallBack = null
            userDataCallback -> userDataCallback = null
            avatarCallback -> avatarCallback = null
            bookShelfCallback -> bookShelfCallback = null
            usernameCallback -> usernameCallback = null
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
                searchCallback?.onFailure(null)
            }

            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                if (response.body() != null)
                    searchCallback?.onSuccess(response.body()!!)
                else
                    searchCallback?.onFailure(null)
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
                infoCallback?.onFailure(null)
            }

            override fun onResponse(call: Call<BookInfo>, response: Response<BookInfo>) {
                if (response.body() == null)
                    infoCallback?.onFailure(null)
                else {
                    try {
                        response.body()?.let {
                            infoCallback?.onSuccess(it)
                        }
                    } catch (e: JsonSyntaxException) {
                        infoCallback?.onFailure(null)
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
                contentsCallback?.onFailure(null)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null) {
                    try {
                        val str = response.body()!!.string()
                        val json: String = str
                            .replace(",]", "]").replace(",}", "}")
                        contentsCallback?.onSuccess(Gson().fromJson(json, BookContents::class.java))
                    } catch (e: JsonSyntaxException) {
                        contentsCallback?.onFailure(null)
                    }
                } else
                    contentsCallback?.onFailure(null)
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
                    chapterContentCallback?.onFailure(chapterId)
            }

            override fun onResponse(
                call: Call<ChapterContent>,
                response: Response<ChapterContent>
            ) {
                if (response.body() != null) {
                    try {
                        response.body()?.let {
                            chapterContentCallback?.onSuccess(it)
                        }?:if (!slient)
                            chapterContentCallback?.onFailure(chapterId)
                    } catch (e: JsonSyntaxException) {
                        if (!slient)
                            chapterContentCallback?.onFailure(chapterId)
                    }
                } else {
                    if (!slient)
                        chapterContentCallback?.onFailure(chapterId)
                }
            }
        })
    }

    //取消加载章节
    fun cancelLoadChapter() {
        chapterCall?.cancel()
    }


    private var categoryCall: Call<BookStoreItem>? = null
    fun cancelGetCategory() {
        categoryCall?.cancel()
    }

    /**
     * @param categoryId 分类ID
     * @param status 状态
     * @param page 页数
     */
    fun getCategory(categoryId: Int, status: String, page: Int) {
        val request = category.create(Novel::class.java)
        categoryCall = request.getCategory(categoryId, status, page)
        categoryCall?.enqueue(object : Callback<BookStoreItem> {
            override fun onFailure(call: Call<BookStoreItem>, t: Throwable) {
                categoryCallBack?.onFailure(null)
            }

            override fun onResponse(call: Call<BookStoreItem>, response: Response<BookStoreItem>) {
                if (response.body() == null)
                    categoryCallBack?.onFailure(null)
                else {
                    try {
                        response.body()?.let {
                            categoryCallBack?.onSuccess(it)
                        }
                    } catch (e: JsonSyntaxException) {
                        categoryCallBack?.onFailure(null)
                    }
                }
            }

        })
    }

    fun login(username: String, password: String) {
        val request = userData.create(Novel::class.java)
        val call = request.login(username.encode(1), password)
        call.enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                response.body()?.let {
                    if (it.code == 200)
                        userDataCallback?.onSuccess(it.apply {
                            this.username = this.username?.decode(1)
                            this.nick = this.nick?.decode(1)
                            this.email = this.email?.decode(1)
                            this.token = this.token?.decode(1)
                        })
                    else
                        userDataCallback?.onFailure(null)
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                userDataCallback?.onFailure(null)
            }
        })
    }

    fun register(username: String, password: String) {
        val request = userData.create(Novel::class.java)
        val call = request.register(username.encode(1), password)
        call.enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                response.body()?.let {
                    if (it.code == 200)
                        userDataCallback?.onSuccess(it.apply {
                            this.username = this.username?.decode(1)
                            this.token = this.token?.decode(1)
                        })
                    else
                        userDataCallback?.onFailure(null)
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                userDataCallback?.onFailure(null)
            }
        })
    }

    fun pullAvatar(token: String) {
        val request = userData.create(Novel::class.java)
        val call = request.pullAvatar(token.decode(2))
        call.enqueue(object : Callback<Avatar> {
            override fun onResponse(call: Call<Avatar>, response: Response<Avatar>) {
                response.body()?.let {
                    avatarCallback?.onSuccess(it)
                }
            }

            override fun onFailure(call: Call<Avatar>, t: Throwable) {
                avatarCallback?.onFailure(null)
            }
        })
    }

    fun pushAvatar(token: String, avatar: String) {
        val request = userData.create(Novel::class.java)
        val call = request.pushAvatar(token.decode(1), avatar)
        call.enqueue(object : Callback<Avatar> {
            override fun onResponse(call: Call<Avatar>, response: Response<Avatar>) {
                response.body()?.let {
                    avatarCallback?.onSuccess(it)
                }
            }

            override fun onFailure(call: Call<Avatar>, t: Throwable) {
                avatarCallback?.onFailure(null)
            }
        })
    }

    fun pushBookShelf(token: String, data: String) {
        val request = userData.create(Novel::class.java)
        val call = request.pushData(token.decode(1), data.encode(2))
        call.enqueue(object : Callback<BookShelf> {
            override fun onResponse(call: Call<BookShelf>, response: Response<BookShelf>) {
                response.body()?.let {
                    if (it.code == 200)
                        bookShelfCallback?.onSuccess(it)
                    else bookShelfCallback?.onFailure(null)
                }
            }

            override fun onFailure(call: Call<BookShelf>, t: Throwable) {
                bookShelfCallback?.onFailure(null)
            }
        })
    }

    fun pullBookShelf(token: String) {
        val request = userData.create(Novel::class.java)
        val call = request.pullData(token.decode(1))
        call.enqueue(object : Callback<BookShelf> {
            override fun onResponse(call: Call<BookShelf>, response: Response<BookShelf>) {
                response.body()?.let {
                    if (it.code == 200) {
                        it.data = it.data?.decode(1)
                        bookShelfCallback?.onSuccess(it)
                    } else
                        bookShelfCallback?.onFailure(null)
                }
            }

            override fun onFailure(call: Call<BookShelf>, t: Throwable) {
                bookShelfCallback?.onFailure(null)
            }
        })
    }

    fun change(username: String, password: String, newPassword: String) {
        val request = userData.create(Novel::class.java)
        val call = request.change(username.encode(1), password, newPassword)
        call.enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                response.body()?.let {
                    if (it.code == 200)
                        userDataCallback?.onSuccess(it.apply {
                            this.username = this.username?.decode(2)
                        })
                    else
                        userDataCallback?.onFailure(null)
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                userDataCallback?.onFailure(null)
            }
        })
    }

    fun change(token: String, nick: String, email: String, avatar: String) {
        val request = userData.create(Novel::class.java)
        val call = request.change(token.decode(1), nick.encode(2), email.encode(2), avatar)
        call.enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                response.body()?.let {
                    if (it.code == 200) {
                        userDataCallback?.onSuccess(it.apply {
                            this.nick = this.nick?.decode(1)
                            this.email = this.email?.decode(1)
                        })
                    } else {
                        userDataCallback?.onFailure(null)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                userDataCallback?.onFailure(null)
            }
        })
    }

    fun checkName(username: String) {
        val request = userData.create(Novel::class.java)
        val call = request.checkName(username.encode(1))

        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                response.body()?.let {
                    if (it.code == 200)
                        usernameCallback?.onSuccess(it)
                    else
                        usernameCallback?.onFailure(null)
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                usernameCallback?.onFailure(null)
            }
        })
    }
}