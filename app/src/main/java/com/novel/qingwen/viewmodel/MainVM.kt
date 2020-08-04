package com.novel.qingwen.viewmodel

import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.novel.qingwen.view.adapter.FragmentAdapter

class MainVM(): ViewModel() {

//    val adapter = FragmentAdapter()
    @BindingAdapter(
        "app:viewPagerAdapter"
    )fun setViewPagerAdapter(view:ViewPager2,adapter:FragmentStateAdapter){
        view.adapter = adapter
    }
}