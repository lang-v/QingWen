package com.novel.qingwen.manager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CustomLinearLayoutManager extends LinearLayoutManager {

    private boolean isScrollEnabled = true;

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }


    //    java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionVH{d5b8e78 position=1 id=-1, oldPos=0, pLpos:0 scrap [attachedScrap] tmpDetached no parent} androidx.recyclerview.widget.RecyclerView{11c99cb VFE...... ......I. 0,69-1080,2170 #7f0a0138 app:id/readList}, adapter:com.novel.qingwen.view.adapter.ReadListAdapter@e0b85a8, layout:com.novel.qingwen.manager.CustomLinearLayoutManager@59148c1, context:com.novel.qingwen.view.activity.ReadActivity@9de65b9
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}
