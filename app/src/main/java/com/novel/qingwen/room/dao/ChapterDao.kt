package com.novel.qingwen.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.novel.qingwen.room.entity.Chapter

@Dao
interface ChapterDao {

    @Query("select * from Chapter where id=:chapterId")
    fun loadById(chapterId:Long):Chapter?

    @Query("update Chapter set content=:newContent where id=:chapterId")
    fun update(chapterId: Long,newContent: String)

    @Query("update Chapter set nextChapter=:nextId where id=:chapterId")
    fun update(chapterId: Long,nextId:Long)

    @Query("update Chapter set nextChapter=:nextId ,content=:newContent where id=:chapterId")
    fun update(chapterId: Long,newContent: String,nextId:Long)

    @Delete
    fun delete(chapter: Chapter)

    @Insert
    fun insertAll(vararg chapter: Chapter)
}