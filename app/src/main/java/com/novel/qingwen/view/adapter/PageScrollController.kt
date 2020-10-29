package com.novel.qingwen.view.adapter

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
     *  1 : pause
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
                            view?.scrollY = view?.scrollY?.plus(1)!!
                        }
                        delay((32 * ((200 - v) / 200)).toLong())
                    }
                    1 -> {
                        Thread.yield()
                    }

                    2 -> {
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
            100 - value
        else 50
    }

    //加速
    fun addV(): Int {
        v -= 2
        if (v <= 50) v = 0
        return 100 - v
    }

    fun reduceV(): Int {
        v += 2
        if (v >= 100) v = 100
        return 100 - v
    }

    fun start() {
        synchronized(runState) {
            //任务正在执行
            if (runState == 0) return
            runState = 0
            job.invoke()
        }
    }

    fun pause() {
        synchronized(runState) {
            runState = 1
        }
    }

    fun resume() {
        synchronized(runState) {
            runState = 0
        }
    }

    fun stop() {
        synchronized(runState) {
            runState = 2
        }
    }

}