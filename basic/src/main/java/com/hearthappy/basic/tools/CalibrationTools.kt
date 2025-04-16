package com.hearthappy.basic.tools

import java.util.regex.Pattern

/**
 * Created Date: 2025/3/25
 * @author ChenRui
 * ClassDescription：校验工具类
 */
object CalibrationTools {
    const val REGEX_USERNAME = "^[A-Za-z0-9._~!@#$^&*]{4,20}$"
    const val REGEX_PASSWORD = "[0-9a-zA-Z_]{6,18}"
    const val REGEX_PHONE = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$"

    /**
     * 匹配验证
     * @param regex String 正则表达式
     * @param content String 待匹配内容
     * @return Boolean
     */
    fun match(regex: String, content: String): Boolean {
        return Pattern.compile(regex).matcher(content).matches()
    }
}