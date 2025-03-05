package com.hearthappy.base.ext

import android.content.res.Resources
import kotlin.math.roundToInt


fun Int.sp2px(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
}

fun Float.sp2px(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
}

fun Int.dp2px(): Float {
    return Resources.getSystem().displayMetrics.density * this
}

fun Float.dp2px(): Float {
    return Resources.getSystem().displayMetrics.density * this
}

fun Int.px2dp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)

}

fun Float.px2dp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)
}