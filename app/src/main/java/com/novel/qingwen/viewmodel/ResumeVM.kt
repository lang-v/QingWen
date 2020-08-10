package com.novel.qingwen.viewmodel

import android.widget.ImageView
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.bean.BookInfo
import com.novel.qingwen.blur.BlurTransformation
import com.novel.qingwen.net.NetUtil
import com.novel.qingwen.net.callback.ResponseCallback


class ResumeVM : BaseVM(), ResponseCallback<BookInfo> {
    var info: Info = Info()

    class Info : BaseObservable() {
        var img: String = ""
            set(value) {
                field = value
                notifyChange()
            }
        var name: String = ""
        var author: String = ""
        var type: String = ""
        var status: String = ""
        var score: String = ""

        var resumeText: String = ""
        var lastChapterTime: String = ""
        var lastChapterName: String = ""

        var id:Long = 0L
    }

    init {
        NetUtil.setInfo(this)
    }

    fun load(id: Long) {
        NetUtil.getBookInfo(id)
    }

    companion object {
        @JvmStatic
        @BindingAdapter("app:imageUrl")
        fun loadImage(
            imageView: ImageView,
            url: String
        ) {
            Glide.with(imageView.context)
                .load(url)
                .into(imageView)
        }

        /**
         * 高斯模糊
         */
        @JvmStatic
        @BindingAdapter("app:blurUrl")
        fun loadBlurImage(view: ImageView, url: String) {
            Glide.with(view.context)
                .asBitmap()
                .apply(RequestOptions.bitmapTransform(BlurTransformation(view.context, 25, 8)))
                .load(url)
                .into(view)
        }
//                .into(object :SimpleTarget<Bitmap>(){
//                    override fun onResourceReady(
//                        resource: Bitmap,
//                        transition: Transition<in Bitmap>?
//                    ) {
//                        view.background = BitmapDrawable(resource)
//                    }
//                })
//        }
    }

    override fun onFailure() {
        iView?.showMsg("发生错误，请检查网络。")
    }

    override fun onSuccess(t: BookInfo) {
        info.id = t.data.Id.toLong()
        info.img = "https://imgapixs.pysmei.com/BookFiles/BookImages/${t.data.Img}"
        info.name = t.data.Name
        info.author = "作者：${t.data.Author}"
        info.type = "类型：${t.data.CName}"
        info.score = "评分：${t.data.BookVote.Score}"
        info.status = "状态：${t.data.BookStatus}"
        info.resumeText = t.data.Desc
        info.lastChapterTime = t.data.LastTime
        info.lastChapterName = t.data.LastChapter
        iView?.onComplete()
    }
    override fun onCleared() {
        NetUtil.clear(this)
        super.onCleared()
    }
}
//id = 501574