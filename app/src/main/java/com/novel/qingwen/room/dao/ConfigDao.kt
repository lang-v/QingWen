package com.novel.qingwen.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.novel.qingwen.room.entity.Config

@Dao
interface ConfigDao {
    @Insert(entity = Config::class)
    fun insert(config:Config)

    @Query("select * from Config where id=:id")
    fun loadById(id:Int = 0):Config

    @Query("update Config set text_size=:textSize ,text_color=:textColor,text_style=:textStyle ,back_ground=:backGround where id=:id")
    fun update(id:Int,textSize:Int,textColor:Int,textStyle:Int,backGround:Int)
}