package com.novel.qingwen.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.novel.qingwen.room.entity.Chapter

@Dao
interface ChapterDao {
    @Query("select * from BookChapter where novelId=:novelId and chapterId=:chapterId")
    fun loadById(novelId:Long,chapterId:Long):Chapter?

    @Query("update BookChapter set content=:newContent where novelId=:novelId and chapterId=:chapterId")
    fun update(novelId:Long,chapterId: Long,newContent: String)

    @Query("update BookChapter set nextChapter=:nextId where novelId=:novelId and chapterId=:chapterId")
    fun update(novelId:Long,chapterId: Long,nextId:Long)

    @Query("update BookChapter set nextChapter=:nextId ,content=:newContent where novelId=:novelId and chapterId=:chapterId")
    fun update(novelId:Long,chapterId: Long,newContent: String,nextId:Long)

    @Delete
    fun delete(chapter: Chapter)

    @Insert
    fun insertAll(vararg chapter: Chapter)
}