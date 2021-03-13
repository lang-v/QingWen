package com.novel.qingwen.manager

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class SlowLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean=false) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    var speedRatio = 0.82f

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return super.scrollHorizontallyBy((dx*speedRatio).toInt(), recycler, state)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return super.scrollVerticallyBy((dy*speedRatio).toInt(), recycler, state)
    }


}