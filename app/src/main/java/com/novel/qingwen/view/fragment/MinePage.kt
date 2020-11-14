package com.novel.qingwen.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.novel.qingwen.R
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.RoomUtil
import com.novel.qingwen.utils.Show
import com.novel.qingwen.utils.UserDataUtil
import com.novel.qingwen.view.activity.Login
import com.novel.qingwen.view.activity.UserInfoActivity
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.fragment_minepage_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MinePage:Fragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_minepage_layout,container,false)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        userTab.setOnClickListener(this)
        logout.setOnClickListener(this)
        mineClearCache.setOnClickListener(this)
        mineCheckNewVersion.setOnClickListener(this)
    }

    override fun onClick(v:View){
        when(v){
            userTab->{
                if (!UserDataUtil.isLogin()){
                    Login.start(requireContext())
                } else {
                    UserInfoActivity.start(this.requireActivity(),mineAvatar)
                }
            }
            logout->{
                UserDataUtil.default.apply {
                    nick = ""
                    email=""
                    password=""
                    token=""
                    avatar=""
                }
                GlobalScope.launch {
                    //删除本地浏览数据
                    RoomUtil.bookInfoDao.deleteAll()
                    synchronized(BookShelfListUtil.getList()) {
                        BookShelfListUtil.getList().clear()
                    }
                    UserDataUtil.update()
                }
                //重置页面
                reset()
            }
            mineClearCache->{
                GlobalScope.launch {
                    val count = RoomUtil.chapterDao.deleteAll()
                    GlobalScope.launch(Dispatchers.Main) {
                        Show.show(requireContext(),"已清除${count}篇小说章节")
                    }
                }
            }
            mineCheckNewVersion->{
                Show.show(requireContext(),"当前：1.1.6r2 已是最新版本")
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        if (UserDataUtil.isLogin())
//            loadUserData()
//        else
//            reset()
    }

    override fun onResume() {
        super.onResume()
        if (UserDataUtil.isLogin())
            loadUserData()
        else
            reset()
    }

    private fun loadUserData(){
        val option = RequestOptions().error(R.mipmap.ic_launcher).transform(RoundedCorners(300))
        Glide.with(mineAvatar)
            .applyDefaultRequestOptions(option)
            .load(UserDataUtil.getAvatar())
            .into(mineAvatar)

        val default = UserDataUtil.default
        if (default.nick == null || default.nick == ""){
            mineNick.text = default.username
        }else{
            mineNick.text = default.nick
        }
        if (default.email != null && default.email != ""){
            mineEmail.text = default.email
        }
        logout.visibility = View.VISIBLE
    }

    private fun reset(){
        mineEmail.text = ""
        mineNick.text = "请登录"
        val option = RequestOptions().error(R.mipmap.ic_launcher).transform(RoundedCorners(300))
        Glide.with(mineAvatar)
            .applyDefaultRequestOptions(option)
            .load(R.mipmap.ic_launcher)
            .into(mineAvatar)
        logout.visibility = View.GONE
    }
}