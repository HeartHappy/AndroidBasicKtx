package com.hearthappy.basic.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.hypot

/**
 * Activity揭露动画显示
 * @receiver Activity
 * @param duration Int
 * @param centerX Int
 * @param centerY Int
 */
fun Activity.createActivityCircularReveal(duration: Int = 200, centerX: Int, centerY: Int) { //设置window背景透明
    //decorView执行动画
    val decorView = window.decorView
    val viewById = decorView.findViewById<View>(android.R.id.content)
    decorView.post {
        val widthPixels = resources.displayMetrics.widthPixels
        val heightPixels = resources.displayMetrics.heightPixels
        val endRadius = hypot(widthPixels.toDouble(), heightPixels.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(viewById, centerX, centerY, 0f, endRadius)
        circularReveal.setDuration(duration.toLong()).start()
    }
}

/**
 * Activity揭露动画消失
 * @receiver Activity
 * @param duration Int
 */
fun Activity.disappearCircularReveal(duration: Int = 200) {
    val decorView = window.decorView
    val viewById = decorView.findViewById<View>(android.R.id.content)
    decorView.post {
        val widthPixels = resources.displayMetrics.widthPixels
        val heightPixels = resources.displayMetrics.heightPixels
        val startRadius = hypot(widthPixels.toDouble(), heightPixels.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(viewById, widthPixels / 2, heightPixels / 2, startRadius, 0f)
        circularReveal.duration = duration.toLong()
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                decorView.rootView.visibility = View.GONE
                finish()
                overridePendingTransition(0, 0) // 禁用默认消失动画
            }
        })
        circularReveal.start()
    }
}