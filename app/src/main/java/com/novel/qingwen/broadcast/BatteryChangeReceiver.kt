package com.novel.qingwen.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BatteryChangeReceiver : BroadcastReceiver() {
    private lateinit var listener: BatteryChangeListener

    override fun onReceive(p0: Context?, p1: Intent?) {
        val level = p1?.getIntExtra("level",0)
        level?.let { listener.change(it) }
    }

    fun setListener(listener:BatteryChangeListener){
        this.listener = listener
    }
}

interface BatteryChangeListener{
    fun change(level: Int)
}