package com.hearthappy.base.ext

import android.text.InputFilter
import android.widget.EditText
import android.widget.Toast


fun EditText.limitText(limitLength: Int = 100, prefix: String = "输入字符不能超过", suffix: String = "个") { //前缀和后缀英文
    val filter = InputFilter { source, start, end, dest, dstart, dend ->
        if (dest.length + (end - start) > limitLength) {
            Toast.makeText(context, prefix.plus(limitLength).plus(suffix), Toast.LENGTH_SHORT).show()
            return@InputFilter ""
        }
        null
    }

    setFilters(arrayOf(filter))
}