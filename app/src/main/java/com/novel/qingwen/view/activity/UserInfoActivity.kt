package com.novel.qingwen.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.UserDataUtil
import com.novel.qingwen.utils.show
import com.novel.qingwen.utils.toBase64
import com.novel.qingwen.viewmodel.UserInfoVM
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class UserInfoActivity : AppCompatActivity(),IBaseView, View.OnClickListener {
    companion object {
        fun start(context: Activity, view: View) {
            val intent = Intent(context, UserInfoActivity::class.java)
            val bundle =
                ActivityOptionsCompat.makeSceneTransitionAnimation(context, view, "mineAvatar")
                    .toBundle()
            context.startActivity(intent, bundle)
        }
    }
    private val viewModel:UserInfoVM by viewModels()
    private var avatar:String= UserDataUtil.default.avatar
    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        window.statusBarColor = Color.parseColor("#669900")
        init()
    }

    private fun init() {
        setSupportActionBar(mineToolbar)
        supportActionBar?.title = "个人信息"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        mineToolbar.setNavigationIcon(R.drawable.back_btn_selector)
        val option = RequestOptions().error(R.mipmap.ic_launcher_round).transform(RoundedCorners(300))
        Glide.with(userInfoAvatar)
            .applyDefaultRequestOptions(option)
            .load(UserDataUtil.getAvatar())
            .error(R.mipmap.ic_launcher_round)
            .into(userInfoAvatar)
        if (UserDataUtil.default.nick != "")
            (userInfoNick as TextView).text = UserDataUtil.default.nick
        if (UserDataUtil.default.email != "")
            (userInfoEmail as TextView).text = UserDataUtil.default.email
        userInfoAvatar.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_info_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }

            R.id.userInfoFinish->{
                viewModel.update(userInfoNick.text.toString(),userInfoEmail.text.toString(),avatar)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }

    private fun startCrop(uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", false)
        intent.putExtra("scale", false)
//        裁剪比例
        intent.putExtra("aspectX",1)
        intent.putExtra("aspectY",1)

//        输出图片大小：
//        intent.putExtra("outputX",100)
//        intent.putExtra("outputY",100)

       
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        startActivityForResult(intent, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            val temp = data?.data
            temp?.let { startCrop(it) }
        }
        if (requestCode == 300 && resultCode == RESULT_OK) {
            val temp = data?.data
            GlobalScope.launch(Dispatchers.Main) {
                val option = RequestOptions().error(R.mipmap.ic_launcher).transform(RoundedCorners(200))
                Glide.with(userInfoAvatar)
                    .applyDefaultRequestOptions(option)
                    .load(temp)
                    .error(R.mipmap.ic_launcher)
                    .into(userInfoAvatar)
            }
            val bitmap = BitmapFactory.decodeStream(temp?.let { contentResolver.openInputStream(it) })
            val t  = bitmap.toBase64()
            t?.let { avatar = t }
//            runOnUiThread {
//                Glide.with(this)
//                    .load(temp)
//                    .override(400, 400)
//                    .into(userInfoAvatar)
//            }
        }
    }

    override fun showMsg(msg: String) {
        show("更新失败")
    }

    override fun onComplete(target: Int, target2: Int) {
        finish()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.userInfoAvatar->{
                openGallery()
            }
        }
    }
}