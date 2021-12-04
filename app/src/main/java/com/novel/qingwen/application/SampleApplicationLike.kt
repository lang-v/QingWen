package com.novel.qingwen.application

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.multidex.MultiDex
import com.novel.qingwen.utils.RoomUtil
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.beta.tinker.TinkerManager
import com.tencent.tinker.entry.DefaultApplicationLike

@Keep
class SampleApplicationLike : DefaultApplicationLike {

    constructor(
        application: Application,
        tinkerFlags: Int,
        tinkerLoadVerifyFlag: Boolean,
        applicationStartElapsedTime: Long,
        applicationStartMillisTime: Long,
        tinkerResultIntent: Intent
    )
            : super(
        application,
        tinkerFlags,
        tinkerLoadVerifyFlag,
        applicationStartElapsedTime,
        applicationStartMillisTime,
        tinkerResultIntent
    );


    override fun onCreate() {
        super.onCreate()
        //初始化SQLite
        RoomUtil.init(this.application)
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onBaseContextAttached(base: Context?) {
        super.onBaseContextAttached(base)
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base)

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun registerActivityLifecycleCallback(callbacks: Application.ActivityLifecycleCallbacks?) {
        application.registerActivityLifecycleCallbacks(callbacks)
    }
}