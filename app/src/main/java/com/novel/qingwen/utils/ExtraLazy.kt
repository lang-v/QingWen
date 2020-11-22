package com.novel.qingwen.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlin.reflect.KProperty

class ExtraLazy<T>(private val extraName: String, private val default: T) {
    private var extra: T? = null
    operator fun getValue(thisRef: AppCompatActivity, property: KProperty<*>):T {
        extra = getExtra(extra,extraName,thisRef)
        return extra?:default
    }

    operator fun getValue(thisRef: Fragment, property: KProperty<*>):T {
        extra = getExtra(extra,extraName,thisRef)
        return extra?:default
    }


    @Suppress("UNCHECKED_CAST")
    private fun getExtra(oldExtra: T?, extraName: String, thisRef: AppCompatActivity): T? =
        oldExtra ?: thisRef.intent?.extras?.get(extraName) as T?

    @Suppress("UNCHECKED_CAST")
    private fun getExtra(oldExtra: T?, extraName: String, thisRef: Fragment): T? =
        oldExtra ?: thisRef.arguments?.get(extraName) as T?
}