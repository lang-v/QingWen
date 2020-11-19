package com.novel.qingwen.view.activity

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.UserDataUtil
import com.novel.qingwen.viewmodel.LoginVM
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.password
import kotlinx.android.synthetic.main.activity_login.username

class Login : AppCompatActivity(), View.OnClickListener,IBaseView {
    companion object{
        fun start(context:Context){
            val intent = Intent(context,Login::class.java)
            context.startActivity(intent)
        }

        fun start(context: Activity, view:View){
            val intent = Intent(context,Register::class.java)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(context,view,"loginBtn").toBundle()
            context.startActivity(intent,bundle)
        }
    }
    private val defaultUser = UserDataUtil.default
    private val viewModel: LoginVM by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.statusBarColor = Color.parseColor("#669900")
        init()
    }

    private fun init(){
        loginText.text = getSpannable()
        loginText.movementMethod = LinkMovementMethod.getInstance()
        loginBack.setOnClickListener(this)
        login.setOnClickListener(this)
        if (defaultUser.username!=null && defaultUser.username != "")
            (username as TextView).text = defaultUser.username
        username.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val name = s?.toString()
                if (name == null || name == ""){
                    loginTips.text = ""
                    loginTips.visibility = View.GONE
                    return
                }
            }
        })
    }

    private fun getSpannable():SpannableString{
        val str = SpannableString("没有账号？点击注册")
        str.setSpan(object:ClickableSpan(){
            override fun onClick(widget: View) {
                Register.start(this@Login)
                finish()
            }
        },5,9,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        str.setSpan(ForegroundColorSpan(Color.RED),5,9,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return str
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.loginBack->{
                onBackPressed()
            }
            R.id.login->{
                val username = username.text.toString()
                val password = password.text.toString()
                if (username != null && password!=null && username != "" && password != "") {
                    viewModel.login(username, password)
                }else{
                    showTips("请补全输入")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    override fun showMsg(msg: String) {
        show(msg)
    }

    override fun onComplete(target: Int, target2: Int) {
        //更新书架列表
        BookShelfListUtil.pullData { BookShelfListUtil.pushData() }
        onBackPressed()
    }

    private val animation = ValueAnimator.ofFloat(-20f,20f)
    private var loginTipsX = 0f
    private fun animationInit(){
        animation.duration = 50
        loginTipsX = loginTips.x
        animation.repeatCount = 3
        animation.repeatMode= ValueAnimator.REVERSE
        animation.addUpdateListener {
            loginTips.x = loginTipsX+(it.animatedValue as Float)
        }
    }
    private fun showTips(msg:String){
        if (loginTipsX==0f){
            animationInit()
        }
        loginTips.text = msg
        loginTips.visibility = View.VISIBLE
        animation.start()
    }
}