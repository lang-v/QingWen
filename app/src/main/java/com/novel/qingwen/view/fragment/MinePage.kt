package com.novel.qingwen.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
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
import com.novel.qingwen.view.activity.DownloadPage
import com.novel.qingwen.view.activity.Login
import com.novel.qingwen.view.activity.UserInfoActivity
import com.novel.qingwen.view.dialog.NoticeDialog
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.fragment_minepage_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.logging.Handler
import java.util.logging.LogRecord

class MinePage : Fragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_minepage_layout, container, false)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        userTab.setOnClickListener(this)
        logout.setOnClickListener(this)
        download.setOnClickListener(this)
        mineClearCache.setOnClickListener(this)
        mineCheckNewVersion.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            userTab -> {
                if (!UserDataUtil.isLogin()) {
                    Login.start(requireContext())
                } else {
                    UserInfoActivity.start(this.requireActivity(), mineAvatar)
                }
            }
            logout -> {
                AlertDialog.Builder(ContextThemeWrapper(requireContext(), R.style.CommonDialog))
                    .setTitle("确认退出登录吗？")
                    .setPositiveButton(
                        "确认"
                    ) { _, _ ->
                        GlobalScope.launch {
                            UserDataUtil.default.apply {
                                nick = ""
                                email = ""
                                password = ""
                                token = ""
                                avatar = ""
                            }
                            UserDataUtil.update()
                        }
                        //重置页面
                        reset()
                    }.setNegativeButton("算了") { _, _ ->
                    }.show()

//                GlobalScope.launch {
//                    //删除本地浏览数据
//                    RoomUtil.bookInfoDao.deleteAll()
//                    synchronized(BookShelfListUtil.getList()) {
//                        BookShelfListUtil.getList().clear()
//                    }
//                    UserDataUtil.update()
//                }
            }
            mineClearCache -> {
                AlertDialog.Builder(ContextThemeWrapper(requireContext(), R.style.CommonDialog))
                    .setTitle("确认清空缓存吗？")
                    .setPositiveButton(
                        "确认"
                    ) { _, _ ->
                        GlobalScope.launch {
                            val count = RoomUtil.chapterDao.deleteAll()
                            GlobalScope.launch(Dispatchers.Main) {
                                Show.show(requireContext(), "已清除${count}篇小说章节")
                            }
                        }
                    }.setNegativeButton("算了") { _, _ ->
                    }.show()
            }
            mineCheckNewVersion -> {
                Show.show(requireContext(), "当前：2.0.0 已是最新版本")
            }
            //进入下载页面
            download->{
                startActivity(Intent(requireContext(),DownloadPage::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        if (UserDataUtil.isLogin())
//            loadUserData()
//        else
//            reset()
        if (UserDataUtil.isWaitRefresh()){
            loadUserData()
        }
    }

    override fun onResume() {
        super.onResume()
        if (UserDataUtil.isLogin())
            loadUserData()
        else
            reset()
    }

    private fun loadUserData() {
        val option = RequestOptions().error(R.mipmap.ic_launcher_round).transform(RoundedCorners(300))
        Glide.with(mineAvatar)
            .applyDefaultRequestOptions(option)
            .load(UserDataUtil.getAvatar())
            .into(mineAvatar)

        val default = UserDataUtil.default
        if (default.nick == "null" || default.nick == "") {
            mineNick.text = default.username
        } else {
            mineNick.text = default.nick
        }
        if (default.email != "null" && default.email != "") {
            mineEmail.text = default.email
        }
        logout.visibility = View.VISIBLE
    }

    private fun reset() {
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