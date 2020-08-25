package com.novel.qingwen.room.dao

import androidx.room.*
import com.novel.qingwen.room.entity.BookInfo

@Dao
interface BookInfoDao {
    @Query("select * from book_info")
    fun loadAll():Array<BookInfo>

    @Update
    fun update(vararg bookInfo: BookInfo):Int

    @Insert
    fun insert(vararg bookInfo: BookInfo)

    @Delete
    fun delete(vararg bookInfo: BookInfo):Int

    @Query("delete from book_info where novelId=:id")
    fun deleteById(id:Long)

}