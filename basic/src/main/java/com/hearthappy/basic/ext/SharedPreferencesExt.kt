package com.hearthappy.basic.ext

import android.content.Context
import android.content.SharedPreferences

/**
 * Created Date 2021/1/20.
 * @author ChenRui
 * ClassDescription:SP工具类
 */

inline fun Context.editApplySaveByName(spFileName: String, block: (edit: SharedPreferences.Editor) -> Unit) {
    val sp = this.getSharedPreferences(spFileName, Context.MODE_PRIVATE)
    val edit = sp.edit()
    block(edit)
    edit.apply()
}

inline fun Context.editCommitSaveByName(spFileName: String, block: (edit: SharedPreferences.Editor) -> Unit): Boolean {
    val sp = this.getSharedPreferences(spFileName, Context.MODE_PRIVATE)
    val edit = sp.edit()
    block(edit)
    return edit.commit()
}


inline fun Context.getSharedPreferences(spFileName: String, block: (sp: SharedPreferences) -> Unit) {
    block(this.getSharedPreferences(spFileName, Context.MODE_PRIVATE))
}

infix fun SharedPreferences.toInt(key: String): Int {
    return this.getInt(key, 0)
}

infix fun SharedPreferences.toString(key: String): String {
    this.getString(key, "")?.let {
        return it
    } ?: return ""
}

infix fun SharedPreferences.toLong(key: String): Long {
    return this.getLong(key, 0L)
}

infix fun SharedPreferences.toFloat(key: String): Float {
    return this.getFloat(key, 0f)
}

infix fun SharedPreferences.toBoolean(key: String): Boolean {
    return this.getBoolean(key, false)
}
