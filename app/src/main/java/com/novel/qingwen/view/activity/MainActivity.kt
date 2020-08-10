package com.novel.qingwen.view.activity

import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.novel.qingwen.R
import com.novel.qingwen.databinding.ActivityMainBinding
import com.novel.qingwen.room.RoomUtil
import com.novel.qingwen.view.adapter.FragmentAdapter
import com.novel.qingwen.view.fragment.SearchBook
import com.novel.qingwen.viewmodel.MainVM
import com.novel.qingwen.utils.Show
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val mainVM:MainVM by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //启动页
        setContentView(R.layout.activity_welcome)
        GlobalScope.launch(Dispatchers.Main) {
            delay(600)//do something
            window.statusBarColor = Color.parseColor("#669900")
            val binding:ActivityMainBinding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)
            binding.lifecycleOwner = this@MainActivity
            binding.mainVM = mainVM
            val list = ArrayList<Fragment>()
            list.add(SearchBook())
            val adapter = FragmentAdapter(this@MainActivity,list)
            viewPager.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        //setContentView(R.fragment_search_list_item.activity_main)
    }


}
infix fun Activity.showError(msg:String){
    Show.show(this,msg,Show.ERROR)
}
infix fun Activity.showSuccess(msg:String){
    Show.show(this,msg,Show.RIGHT)
}
infix fun Activity.show(msg: String){
    Show.show(this,msg,Show.NONE)
}
