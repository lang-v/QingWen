package com.novel.qingwen.application

import android.app.Application
import android.text.TextUtils
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.ConfigUtil
import com.novel.qingwen.utils.RoomUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
            //初始化SQLite
            RoomUtil.init(this)
            GlobalScope.launch(Dispatchers.Default) {
                ConfigUtil.init()
                BookShelfListUtil.init()
            }
//        GlobalScope.launch {
//            RoomUtil.chapterDao.insertAll(Chapter(11L,"001 重生剑客","那一天，我重生了。",-1,-1))
//            val t = RoomUtil.chapterDao.loadById(11L)
//            Log.e("Application","t=$t")
//        }
//        CrashReport.testJavaCrash()
        }


    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName: String = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }
}