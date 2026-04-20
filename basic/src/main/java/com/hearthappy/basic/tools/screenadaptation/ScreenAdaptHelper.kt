package com.hearthappy.basic.tools.screenadaptation


import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics

object ScreenAdaptHelper {

    // 设计稿尺寸 (以 1080p 为基准，宽度 360dp 或 1080px)
    private const val DESIGN_WIDTH_DP = 360f

    private var nonCompatDensity = 0f
    private var nonCompatScaledDensity = 0f
    private var appMetrics: DisplayMetrics? = null

    private var designWidthDp = DESIGN_WIDTH_DP

    fun setup(application: Application, designWidthDp: Float = DESIGN_WIDTH_DP) {
        this.designWidthDp = designWidthDp
        appMetrics = application.resources.displayMetrics
        val displayMetrics = application.resources.displayMetrics

        if (nonCompatDensity == 0f) {
            nonCompatDensity = displayMetrics.density
            nonCompatScaledDensity = displayMetrics.scaledDensity // 防止系统切换字体大小导致适配失效
            application.registerComponentCallbacks(object: ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    if (newConfig.fontScale > 0) {
                        nonCompatScaledDensity = application.resources.displayMetrics.scaledDensity
                    }
                }

                override fun onLowMemory() {}
            })
        }
    }

    fun adapt(activity: Activity) {
        val appDisplayMetrics = activity.application.resources.displayMetrics
        val activityDisplayMetrics = activity.resources.displayMetrics

        // 1. 计算目标密度 (以宽度为基准进行适配)
        val targetDensity = appDisplayMetrics.widthPixels / designWidthDp
        val targetScaledDensity = targetDensity * (nonCompatScaledDensity / nonCompatDensity)
        val targetDensityDpi = (targetDensity * 160).toInt()

        // 2. 替换 Application 的密度
        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaledDensity
        appDisplayMetrics.densityDpi = targetDensityDpi

        // 3. 替换当前 Activity 的密度
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.scaledDensity = targetScaledDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
    }

    // 提供给扩展函数使用的属性
    val displayMetrics: DisplayMetrics
        get() = appMetrics ?: Resources.getSystem().displayMetrics
}