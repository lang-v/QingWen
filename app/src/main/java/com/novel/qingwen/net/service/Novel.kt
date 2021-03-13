package com.novel.qingwen.net.service

import com.novel.qingwen.net.bean.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface Novel {
    @HTTP(method = "GET", path = "BookFiles/Html/{pageid}/{novelid}/info.html", hasBody = false)
    suspend fun getBookInfo(
        @Path("novelid") novelId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): BookInfo

    @HTTP(
        method = "GET",
        path = "BookFiles/Html/{pageid}/{novelid}/{chapterid}.html",
        hasBody = false
    )
    suspend fun getChapterContent(
        @Path("novelid") novelId: Long,
        @Path("chapterid") chapterId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): ChapterContent

    @HTTP(
        method = "GET",
        path = "BookFiles/Html/{pageid}/{novelid}/{chapterid}.html",
        hasBody = false
    )
    fun downloadChapter(
        @Path("novelid") novelId: Long,
        @Path("chapterid") chapterId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): Call<ChapterContent>


    /**
     * 坑死了，json数据格式不标准，这里做修改，对json数据做预处理，再转POJO
     */
    @HTTP(method = "GET", path = "BookFiles/Html/{pageid}/{novelid}/index.html", hasBody = false)
    suspend fun getContents(
        @Path("novelid") novelId: Long,
        @Path("pageid") pageId: Long = (novelId / 1000 + 1)
    ): ResponseBody

    /**
     * @param id 此处固定为app2
     */
    @GET("search.aspx")
    suspend fun search(
        @Query("key") key: String,
        @Query("page") page: Long,
        @Query("siteid") id: String = "app2"
    ): SearchResult

    /**
     * 获取分类下的小说
     */
    @HTTP(method = "GET", path = "Categories/{categoryId}/{status}/{page}.html")
    suspend fun getCategory(
        @Path("categoryId") categoryId: Int,
        @Path("status") status: String,
        @Path("page") page: Int
    ):BookStoreItem

    @FormUrlEncoded
    @POST("login")
    fun login(@Field("username") username:String,@Field("password") password:String):Call<LoginResult>

    @FormUrlEncoded
    @POST("register")
    fun register(@Field("username") username:String,@Field("password") password:String):Call<LoginResult>

    @FormUrlEncoded
    @POST("pullavatar")
    fun pullAvatar(@Field("token") token:String):Call<Avatar>

    @FormUrlEncoded
    @POST("pushavatar")
    fun pushAvatar(@Field("token")token:String,@Field("avatar")avatar:String):Call<Avatar>

    @FormUrlEncoded
    @POST("pushdata")
    fun pushData(@Field("token")token: String,@Field("data") data:String):Call<BookShelf>

    @FormUrlEncoded
    @POST("pulldata")
    fun pullData(@Field("token")token: String):Call<BookShelf>

    @FormUrlEncoded
    @POST("changepassword")
    fun change(@Field("username") username:String,
               @Field("password") password: String,
               @Field("newPassword") newPassword: String):Call<LoginResult>

    @FormUrlEncoded
    @POST("changeuserinfo")
    fun change(@Field("token") token:String?,
               @Field("nick") nick: String?,
               @Field("email") email:String?,
               @Field("avatar") avatar:String?):Call<LoginResult>

    @GET("checkname")
    fun checkName(@Query("username")username: String):Call<BaseResponse>
}