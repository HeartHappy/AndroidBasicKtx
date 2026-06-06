package com.hearthappy.basic.ext

import android.graphics.Color
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

/**
 * 数量转换
 * @receiver Int
 * @param hintText String 赞、评论等提示文字
 * @param limitNumber Int 超过99时显示99+
 * @return String 显示的文字
 */
fun Int.showTextOrNumber(hintText: String, limitNumber: Int = 99): String {
    return if (this == 0) {
        hintText
    } else if (this > limitNumber) {
        limitNumber.toString().plus("+")
    } else {
        this.toString()
    }
}

/**
 * 辅助扩展：调整颜色透明度
 *
 * @param factor 0.5
 * @return 返回透明颜色
 */
private fun Int.adjustAlpha(factor: Float): Int {
    val alpha = (Color.alpha(this) * factor).toInt()
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}