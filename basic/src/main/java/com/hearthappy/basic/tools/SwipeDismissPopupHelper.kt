package com.hearthappy.basic.tools

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import kotlin.math.abs
/**
 * Created Date: 2025/7/9
 * @author ChenRui
 * ClassDescription：window侧滑消失工具类
 */
class SlideDismissPopupHelper(private val popupWindow: PopupWindow, private val contentView: View, private val slideDirection: SlideDirection = SlideDirection.HORIZONTAL) {


    // 记录触摸起点
    private var startX = 0f
    private var startY = 0f
    private var duration = 200L

    // 记录滑动过程中的实时位移 // 水平：translationX；垂直：translationY
    private var currentTranslation = 0f

    // PopupWindow 宽/高（用于计算阈值）
    private val popupSize by lazy { // 水平方向用宽度
        if (slideDirection == SlideDirection.HORIZONTAL) {
            contentView.width
        } else { // 垂直方向用高度
            contentView.height
        }
    }

    // 动画插值器
    private val interpolator = DecelerateInterpolator(1.5f)

    @SuppressLint("ClickableViewAccessibility") fun attach() {
        contentView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { // 记录按下时的起点
                    startX = event.rawX
                    startY = event.rawY
                    currentTranslation = if (slideDirection == SlideDirection.HORIZONTAL) {
                        contentView.translationX // 水平方向：记录当前X位移
                    } else {
                        contentView.translationY // 垂直方向：记录当前Y位移
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> { // 根据方向计算滑动距离
                    val slideDistance = if (slideDirection == SlideDirection.HORIZONTAL) { // 水平：计算X方向滑动距离
                        event.rawX - startX + currentTranslation
                    } else { // 垂直：计算Y方向滑动距离（向上为负，向下为正）
                        event.rawY - startY + currentTranslation
                    }

                    // 限制滑动范围（避免过度滑动）
                    val limitedDistance = if (slideDirection == SlideDirection.HORIZONTAL) {
                        slideDistance.coerceIn(-popupSize.toFloat(), popupSize.toFloat()) // 水平：左右限制
                    } else {
                        slideDistance.coerceIn(-popupSize.toFloat() * 2, popupSize.toFloat() / 2) // 垂直：向上限制更多
                    }

                    // 1. 跟随移动：更新视图位移
                    if (slideDirection == SlideDirection.HORIZONTAL) {
                        contentView.translationX = limitedDistance
                    } else {
                        contentView.translationY = limitedDistance
                    }

                    // 2. 随滑动改变透明度（增强视觉反馈）
                    val slideRatio = abs(limitedDistance) / popupSize
                    val alphaRatio = 1 - slideRatio / (DISMISS_RATIO * 2)
                    contentView.alpha = alphaRatio.coerceAtLeast(0.3f) // 最低保留30%透明度

                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { // 计算最终滑动距离与阈值的比例
                    val slideDistance = if (slideDirection == SlideDirection.HORIZONTAL) {
                        abs(contentView.translationX)
                    } else {
                        abs(contentView.translationY) // 垂直方向取绝对值
                    }
                    val slideRatio = slideDistance / popupSize

                    if (slideRatio > DISMISS_RATIO) { // 超过阈值：执行销毁动画
                        if (slideDirection == SlideDirection.HORIZONTAL) { // 水平：向滑动方向滑出屏幕
                            val targetX = if (contentView.translationX > 0) popupSize.toFloat() else -popupSize.toFloat()
                            contentView.animate().translationX(targetX).alpha(0f).setDuration(duration).setInterpolator(interpolator).withEndAction { popupWindow.dismiss() }.start()
                        } else { // 垂直：向上滑出屏幕（通常垂直方向只处理向上滑动销毁）
                            val isSlidingUp = contentView.translationY < 0 // 上滑（translationY为负） // isSlidingUp=true:上滑→向上滑出 反则下滑
                            val targetY = if (isSlidingUp) -popupSize.toFloat() else popupSize.toFloat()
                            contentView.animate().translationY(targetY).alpha(0f).setDuration(duration).setInterpolator(interpolator).withEndAction { popupWindow.dismiss() }.start()
                        }
                    } else { // 未超过阈值：回弹到原位置
                        if (slideDirection == SlideDirection.HORIZONTAL) {
                            contentView.animate().translationX(0f).alpha(1f).setDuration(duration).setInterpolator(interpolator).start()
                        } else {
                            contentView.animate().translationY(0f).alpha(1f).setDuration(duration).setInterpolator(interpolator).start()
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }

    companion object {
        // 滑动阈值：超过宽度/高度的 1/3 则销毁
        private const val DISMISS_RATIO = 1f / 3f
    }
}

enum class SlideDirection {
    HORIZONTAL, // 水平方向（左右滑动）
    VERTICAL,    // 垂直方向（上下滑动）
    NONE //没有任何
}