package com.novel.qingwen.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.lang.ref.WeakReference

class BatteryChangeReceiver : BroadcastReceiver() {
    private var listener: WeakReference<BatteryChangeListener?> = WeakReference(null)

    override fun onReceive(p0: Context?, p1: Intent?) {
        val level = p1?.getIntExtra("level",0)
        level?.let { listener.get()?.change(it) }
    }

    fun setListener(listener:BatteryChangeListener){
        this.listener = WeakReference(listener)
    }
}

interface BatteryChangeListener{
    fun change(level: Int)
}