package com.novel.qingwen.room.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Config")
data class Config(
    @PrimaryKey @NonNull val id: Int,
    @ColumnInfo(name = "text_size") var textSize: Int,
    @ColumnInfo(name = "text_color") var textColor: Int,
    /**
     * 0 :黑体
     * 1 :楷体 (正楷)
     */
    @ColumnInfo(name = "text_style") var textStyle: Int,
    @ColumnInfo(name = "back_ground") var backGround: Int,
    @ColumnInfo(name = "auto_scroll_v",defaultValue = "0") var autoScrollV:Int
)