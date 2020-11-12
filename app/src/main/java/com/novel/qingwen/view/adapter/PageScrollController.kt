package com.novel.qingwen.view.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PageScrollController {
    private var view: RecyclerView? = null
    private var v = 10

    /**
     * -1 : 无状态
     *  0 : start
     *  1 : pause running
     *  2 : stop
     */
    private var runState = -1

    private val job = {
        GlobalScope.launch {
            while (true) {
                when (runState) {
                    -1 -> {
                        return@launch
                    }
                    0 -> {
                        if (view == null) return@launch
                        view?.post {
                            view?.scrollBy(0, 2)
                            //发送已经停止滑动的消息，让自动加载下一章的监听生效
//                            view?.scrollTo(0,2)
//                            view?.setScrollState(RecyclerView.SCROLL_STATE_IDLE)
//                            view?.dispatchOnScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE)
//                            view?.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE)
                        }
                        val time = ((100 - v) / 2).toLong()
                        delay(5 + time)
                    }

                    1, 2 -> {
                        return@launch
                    }
                }
            }
        }
    }

    fun attachView(view: RecyclerView) {
        this.view = view
    }

    fun detachView() {
        view = null
    }

    fun setV(value: Int) {
        v = if (value in 1..100)
            value
        else 50
    }

    //加速
    fun addV(): Int {
        v += 2
        if (v >= 100) v = 100
        return v
    }

    fun reduceV(): Int {
        v -= 2
        if (v <= 0) v = 1
        return v
    }

    fun start() {
        synchronized(runState) {
            //任务正在执行
            if (runState == 0) return
            runState = 0
            job.invoke()
        }
    }

    fun stop() {
        synchronized(runState) {
            runState = 2
        }
    }

    fun pause() {
        synchronized(runState) {
            runState = 1
        }
    }

    fun resume() {
        start()
    }

    fun isPause(): Boolean {
        return runState == 1
    }

    fun isRunning(): Boolean {
        return runState == 0
    }

}