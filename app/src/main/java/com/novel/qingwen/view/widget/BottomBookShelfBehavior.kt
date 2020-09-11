package com.novel.qingwen.view.widget

import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.novel.qingwen.R

class  BottomBookShelfBehavior<V:View>: BottomSheetBehavior<V>() {

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE)
            parent.findViewById<SwipeRefreshLayout>(R.id.bookShelfRefresh).onTouchEvent(event)
        return super.onTouchEvent(parent, child, event)
    }
    fun fromCopy(v:V):BottomSheetBehavior<V>{
        return BottomSheetBehavior.from(v)
    }
}