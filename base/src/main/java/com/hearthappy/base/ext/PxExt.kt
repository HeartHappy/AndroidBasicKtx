package com.hearthappy.base.ext

import android.content.res.Resources
import kotlin.math.roundToInt


fun Int.sp2px(): Int {
    return (this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f).toInt()
}

fun Float.sp2px(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
}

fun Int.dp2px(): Int {
    return (Resources.getSystem().displayMetrics.density * this).toInt()
}

fun Float.dp2px(): Float {
    return Resources.getSystem().displayMetrics.density * this
}

fun Int.px2dp(): Int {
    return ((this / Resources.getSystem().displayMetrics.density).toInt())

}

fun Float.px2dp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)
}