package com.novel.qingwen.net.service

import com.novel.qingwen.bean.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.Query

interface Novel {
    @HTTP(method = "GET",path = "BookFiles/Html/{pageid}/{novelid}/info.html",hasBody = false)
    fun getBookInfo(@Path("novelid") novelId: Long,@Path("pageid") pageId: Long=(novelId/1000 + 1)):Call<BookInfo>

    @HTTP(method = "GET",path = "https://contentxs.pysmei.com/BookFiles/Html/{pageid}/{novelid}/{chapterid}.html",hasBody = false)
    fun getChapterContent(@Path("pageid") pageId: Int,@Path("novelid") novelId: Int,@Path("chapterid") chapterId:Int):Call<ChapterContent>

    @HTTP(method = "GET",path = "https://infosxs.pysmei.com/BookFiles/Html/{pageid}/{novelid}/index.html",hasBody = false)
    fun getContents(@Path("pageid") pageId:Int,@Path("novelid") novelId:Int):Call<BookContents>

    /**
     * @param id 此处固定为app2
     */
    @GET("search.aspx")
    fun search(@Query("key") key:String,@Query("page") page:Int,@Query("siteid") id:String="app2"):Call< SearchResult>
}