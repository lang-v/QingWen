package com.novel.qingwen.application

import android.app.Application
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.utils.UserDataUtil
import com.novel.qingwen.viewmodel.DownloadVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //初始化SQLite
        RoomUtil.init(this)
    }
}