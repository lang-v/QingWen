package com.novel.qingwen.view.activity

import android.animation.ValueAnimator
import android.app.Activity
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
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import com.novel.qingwen.R
import com.novel.qingwen.base.IBaseView
import com.novel.qingwen.utils.BookShelfListUtil
import com.novel.qingwen.utils.show
import com.novel.qingwen.viewmodel.LoginVM
import kotlinx.android.synthetic.main.activity_register.*

class Register : AppCompatActivity(), View.OnClickListener,IBaseView {
    companion object{
        fun start(context: Activity){
            val intent = Intent(context,Register::class.java)
            context.startActivity(intent)
        }


        fun start(context: Activity,view:View){
            val intent = Intent(context,Register::class.java)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(context,view,"loginBtn").toBundle()
            context.startActivity(intent,bundle)
        }
    }

    private val viewModel:LoginVM by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        window.statusBarColor = Color.parseColor("#669900")
        init()
    }
    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachView()
    }

    private fun init(){
        registerText.setText(getSpannable())
        registerText.movementMethod = LinkMovementMethod.getInstance()
        register.setOnClickListener(this)
        registerBack.setOnClickListener(this)
        username.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val name = s?.toString()
                if (name == null || name == ""){
                    registerTips.text = ""
                    registerTips.visibility = View.GONE
                    return
                }
                viewModel.checkName(name)
            }
        })
    }

    private fun getSpannable(): SpannableString {
        val str = SpannableString("已有账号？点击登录")
        str.setSpan(object: ClickableSpan(){
            override fun onClick(widget: View) {
                Login.start(this@Register)
                finish()
            }
        },5,9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        str.setSpan(ForegroundColorSpan(Color.RED),5,9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return str
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.registerBack->{
                onBackPressed()
            }
            R.id.register->{
                val username = username.text.toString()
                val password = password.text.toString()
                if (username != null && password!=null && username != "" && password != "") {
                    viewModel.register(username, password)
                }else{
                    showTips("请补全输入")
                }
            }
        }
    }

    override fun showMsg(msg: String) {
        show(msg)
    }

    override fun onComplete(target: Int, target2: Int) {
        when(target){
            0->{
                registerTips.text = ""
                registerTips.visibility = View.GONE
            }
            1->{
                registerTips.text = "名称不可用"
                registerTips.visibility = View.VISIBLE
            }
            2->{
                //更新书架列表
                BookShelfListUtil.pullData { BookShelfListUtil.pushData() }
                onBackPressed()
            }
        }
    }

    private val animation = ValueAnimator.ofFloat(-20f,20f)
    private var registerTipsX = 0f
    private fun animationInit(){
        animation.duration = 30
        registerTipsX = registerTips.x
        animation.repeatCount = 3
        animation.repeatMode=ValueAnimator.REVERSE
        animation.addUpdateListener {
            registerTips.x = registerTipsX+(it.animatedValue as Float)
        }
    }
    private fun showTips(msg:String){
        if (registerTipsX==0f){
            animationInit()
        }
        registerTips.text = msg
        registerTips.visibility = View.VISIBLE
        animation.start()
    }

}