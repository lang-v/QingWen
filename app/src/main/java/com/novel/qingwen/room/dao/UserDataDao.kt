package com.novel.qingwen.room.dao

import androidx.room.*
import com.novel.qingwen.room.entity.Config
import com.novel.qingwen.room.entity.UserData

@Dao
interface UserDataDao {
    @Insert(entity = UserData::class)
    fun insert(userData: UserData)

    @Query("select * from UserData where id=:id")
    fun loadById(id: Int = 0): UserData

    @Query("update UserData set token=:token, username=:username, nick=:nick,email=:email,avatar=:avatar,password=:password where id=0")
    fun update(
        token: String,
        username: String,
        nick: String,
        email: String,
        avatar: String,
        password: String
    )
}