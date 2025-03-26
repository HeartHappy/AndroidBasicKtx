package com.hearthappy.base.ext

import android.graphics.RectF
import android.view.View

/**
 * Created Date: 2024/12/24
 * @author ChenRui
 * ClassDescription：View扩展类
 */

/**
 * 根据条件是否显示View
 * @receiver T
 * @param conditions Boolean
 * @param showBlock [@kotlin.ExtensionFunctionType] Function1<T, Unit>
 * @param hideBlock [@kotlin.ExtensionFunctionType] Function1<T, Unit>
 */
fun <T : View> T.show(conditions: Boolean, showBlock: T.() -> Unit = {}, hideBlock: T.() -> Unit = {}) {
    if (conditions) {
        showBlock()
        visible()
    } else {
        hideBlock()
        gone()
    }
}

fun View?.visible() {
    this?.let { if (visibility != View.VISIBLE) visibility = View.VISIBLE }
}

fun View?.gone() {
    this?.let { if (visibility != View.GONE) visibility = View.GONE }
}

fun View?.invisible() {
    this?.let { if (visibility != View.INVISIBLE) visibility = View.INVISIBLE }
}

/**
 * (x,y)是否在view的区域内
 *
 * @param view 控件范围
 * @param x    x坐标
 * @param y    y坐标
 * @return 返回true，代表在范围内
 */
fun View?.isTouchPointInView(x: Int, y: Int): Boolean {
    return this?.run {
        val location = IntArray(2)
        getLocationOnScreen(location)
        val rectF = RectF(location[0].toFloat(), location[1].toFloat(), (location[0] + width).toFloat(), (location[1] + height).toFloat())
        rectF.contains(x.toFloat(), y.toFloat())
    } ?: false
}


/**
 * 查找View在窗口中所在位置
 */
fun View.findViewLocation(): RectF {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return RectF(location[0].toFloat(), location[1].toFloat(), (location[0] + width).toFloat(), (location[1] + height).toFloat())
}

/**
 * 获取View的中心坐标
 * @receiver View
 * @return Pair<Float, Float>
 */
fun View.findViewCoordinates(): Pair<Float, Float> {
    val rect = findViewLocation()
    val centerX = (rect.left + rect.right) / 2
    val centerY = (rect.top + rect.bottom) / 2
    return Pair(centerX, centerY)
}

