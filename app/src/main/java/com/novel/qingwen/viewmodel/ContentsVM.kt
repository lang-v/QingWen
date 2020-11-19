package com.novel.qingwen.viewmodel

import com.novel.qingwen.base.BaseVM
import com.novel.qingwen.net.bean.BookContents
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.net.callback.ResponseCallback

class ContentsVM : BaseVM(), ResponseCallback<BookContents> {
    init {
        NetUtil.setContents(this)
    }

    private val list = ArrayList<ContentsInfo>()
    private val headList = ArrayList<ContentsInfo>()

    fun getList(): ArrayList<ContentsInfo> {
        return list
    }

    fun getHeadList(): ArrayList<ContentsInfo> {
        return headList
    }

    fun load(id: Long) {
        NetUtil.getContents(id)
    }

    override fun onFailure() {
        iView?.showMsg("发生错误，请检查网络。")
    }

    override fun onSuccess(t: BookContents) {
//        Log.e("SL", "$t")
        t.data.list.forEach {
            list.add(ContentsInfo(it.name))
            headList.add(ContentsInfo(it.name,list.size.toLong()-1))
            it.list.forEach { it1 ->
                list.add(ContentsInfo(it1.name,it1.id))
            }
        }
        iView?.onComplete(target2 = 0)
    }

    /**
     * 当id = -1 说明是一级目录，反之为二级目录
     */
    class ContentsInfo(var name:String,var id:Long=-1L,var isSelect:Boolean = false)

    override fun onCleared() {
        NetUtil.clear(this)
        super.onCleared()
    }
}