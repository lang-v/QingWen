package com.novel.qingwen

import com.google.gson.Gson
import com.novel.qingwen.net.bean.Avatar
import com.novel.qingwen.net.bean.BookShelf
import com.novel.qingwen.net.bean.LoginResult
import com.novel.qingwen.net.callback.ResponseCallback
import com.novel.qingwen.room.entity.BookInfo
import com.novel.qingwen.utils.NetUtil
import com.novel.qingwen.utils.UserDataUtil
import com.novel.qingwen.utils.decode
import com.novel.qingwen.utils.encode
import kotlinx.coroutines.delay

import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        NetUtil.setAvatar(object : ResponseCallback<Avatar> {
            override fun onFailure() {
                println("failed")
            }

            override fun onSuccess(t: Avatar) {
                val string = Gson().toJson(t)
                println(string)
//                println(string.encode(1))
                println(string.decode(1))
//                println("结果："+str.decode(1))
            }

        })

        NetUtil.setUserData(object : ResponseCallback<LoginResult> {
            override fun onFailure() {
                println("failed")

            }

            override fun onSuccess(t: LoginResult) {
                println("result:" + Gson().toJson(t))
            }

        })
        NetUtil.setBookShelf(object :ResponseCallback<BookShelf>{
            override fun onFailure() {
                println("failed")
            }

            override fun onSuccess(t: BookShelf) {
                println("result:" + Gson().toJson(t).decode(1))
            }
        })
//        NetUtil.pullAvatar("123123")
//        NetUtil.pushAvatar("123123","+++---d石浪...".encode(1))
//        val str = "王中王火腿肠".encode(1)
//        println(str)
        NetUtil.register("Frontman", "ws2240")
//        NetUtil.pushBookShelf(
//            "123123",
//            Gson().toJson(ArrayList<BookInfo>().apply {
//                add(BookInfo(1L,"15","s",false,55L,-5165,"剑尊",516L,5155L,"15:55","新章节"))
//            })
//        )
//        NetUtil.login("王中王火腿肠","abc")
        //eyJub3ciOjE2MDUzMzk0MDQsInJhbmRfbnVtIjoiQUxkIiwiZXhwaXJlIjoxNjM2ODc1NDA0fQ==.mBsTiW3Xw/2qNcZeghYLs5gbE4lt18P9qjXGXoIWC7OYGxOJbdfD/ao1xl6CFguz5RWPo5pJyDGIYZ9FOZ5YPA==.+8HMlgemcbo+HiM8MnYAnw==
//        NetUtil.change("eyJub3ciOjE2MDUzMzk0MDQsInJhbmRfbnVtIjoiQUxkIiwiZXhwaXJlIjoxNjM2ODc1NDA0fQ==.mBsTiW3Xw/2qNcZeghYLs5gbE4lt18P9qjXGXoIWC7OYGxOJbdfD/ao1xl6CFguz5RWPo5pJyDGIYZ9FOZ5YPA==.+8HMlgemcbo+HiM8MnYAnw==","小鸟","qq.com","")

//        NetUtil.pullBookShelf("eyJub3ciOjE2MDUzNDQ0MjksInJhbmRfbnVtIjoiR2JLIiwiZXhwaXJlIjoxNjM2ODgwNDI5fQ==.mBsTiW3Xw/2qNcZeghYLs5gbE4lt18P9qjXGXoIWC7OYGxOJbdfD/ao1xl6CFguz5RWPo5pJyDGIYZ9FOZ5YPA==.+8HMlgemcbo+HiM8MnYAnw==")


//        val string = "+++---d石浪..."
//        println(string)
//        println(string.encode(1))
//        println(string.decode(1))
        while (true) {
            Thread.yield()
        }
    }
}