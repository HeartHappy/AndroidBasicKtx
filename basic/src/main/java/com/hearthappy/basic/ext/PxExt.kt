package com.hearthappy.basic.ext

import com.hearthappy.basic.tools.screenadaptation.ScreenAdaptHelper

// ==========================================================
// 【旧方案】已弃用 - 内部重定向到新方案，确保不再受系统 Density 坑害
// ==========================================================

@Deprecated(message = "由于 Resources.getSystem() 不支持屏幕适配方案，请迁移至 .dp 属性", replaceWith = ReplaceWith("this.dp"))
fun Int.dp2px(): Int = this.dp

@Deprecated(message = "请迁移至 .dp 属性", replaceWith = ReplaceWith("this.dp"))
fun Float.dp2px(): Float = this.dp

@Deprecated(message = "由于 Resources.getSystem() 不支持屏幕适配方案，请迁移至 .sp 属性", replaceWith = ReplaceWith("this.sp"))
fun Int.sp2px(): Int = this.sp

@Deprecated(message = "请迁移至 .sp 属性", replaceWith = ReplaceWith("this.sp"))
fun Float.sp2px(): Float = this.sp

@Deprecated(message = "请迁移至 .px2dp 属性", replaceWith = ReplaceWith("this.px2dp"))
fun Int.px2dp(): Int = this.px2dp.toInt()

@Deprecated(message = "请迁移至 .px2dp 属性", replaceWith = ReplaceWith("this.px2dp"))
fun Float.px2dp(): Float = this / ScreenAdaptHelper.displayMetrics.density

// ==========================================================
// 【新方案】扩展属性 - 简洁、无括号、支持动态适配
// ==========================================================

/** Int 转换为 px (Int) */
val Int.dp: Int
    get() = (this * ScreenAdaptHelper.displayMetrics.density + 0.5f).toInt()

/** Float 转换为 px (Float) */
val Float.dp: Float
    get() = this * ScreenAdaptHelper.displayMetrics.density

/** Int 转换为 sp (Int) */
val Int.sp: Int
    get() = (this * ScreenAdaptHelper.displayMetrics.scaledDensity + 0.5f).toInt()

/** Float 转换为 sp (Float) */
val Float.sp: Float
    get() = this * ScreenAdaptHelper.displayMetrics.scaledDensity

/** px 转换为 dp (Float) */
val Int.px2dp: Float
    get() = this / ScreenAdaptHelper.displayMetrics.density