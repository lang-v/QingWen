package com.novel.qingwen.net.service

import com.novel.qingwen.net.bean.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.Query

interface Novel {
    @HTTP(method = "GET", path = "BookFiles/Html/{pageid}/{novelid}/info.html", hasBody = false)
    fun getBookInfo(
        @Path("novelid") novelId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): Call<BookInfo>

    @HTTP(
        method = "GET",
        path = "BookFiles/Html/{pageid}/{novelid}/{chapterid}.html",
        hasBody = false
    )
    fun getChapterContent(
        @Path("novelid") novelId: Long,
        @Path("chapterid") chapterId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): Call<ChapterContent>

    /**
     * 坑死了，json数据格式不标准，这里做修改，对json数据做预处理，再转POJO
     */
    @HTTP(method = "GET", path = "BookFiles/Html/{pageid}/{novelid}/index.html", hasBody = false)
    fun getContents(
        @Path("novelid") novelId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): Call<ResponseBody>

    /**
     * @param id 此处固定为app2
     */
    @GET("search.aspx")
    fun search(
        @Query("key") key: String,
        @Query("page") page: Long,
        @Query("siteid") id: String = "app2"
    ): Call<SearchResult>

    /**
     * 获取分类下的小说
     */
    @HTTP(method = "GET", path = "Categories/{categoryId}/{status}/{page}.html")
    fun getCategory(
        @Path("categoryId") categoryId: Int,
        @Path("status") status: String,
        @Path("page") page: Int
    ):Call<BookStoreItem>
}