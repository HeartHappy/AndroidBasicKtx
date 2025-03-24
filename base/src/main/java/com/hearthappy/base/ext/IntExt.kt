package com.hearthappy.base.ext

import java.util.Locale

enum class FormatType {
    W_KW, K_W_WK
}

/**
 * 根据指定的格式类型对整数进行格式化
 * @receiver Int 要格式化的整数
 * @param formatType FormatType 格式化类型，可选值为 W_KW 或 K_W_WK
 * @param locale Locale 用于格式化的区域设置，默认为系统默认区域
 * @return String 格式化后的字符串
 */
fun Int.formatNumber(formatType: FormatType, locale: Locale = Locale.getDefault()): String {
    return when (formatType) {
        FormatType.W_KW -> {
            when {
                this >= 10000000 -> String.format(locale, "%.0fkw", this / 10000000.0)
                this >= 10000 -> String.format(locale, "%.0fw", this / 10000.0)
                else -> this.toString()
            }
        }

        FormatType.K_W_WK -> {
            if (this >= 10000000) { // 千万及以上
                String.format(locale, "%.1fkw", this / 10000000.0)
            } else if (this >= 10000) { // 万及以上
                String.format(locale, "%.1fw", this / 10000.0)
            } else if (this >= 1000) { // 千及以上
                String.format(locale, "%.1fk", this / 1000.0)
            } else {
                this.toString()
            }
        }
    }
}