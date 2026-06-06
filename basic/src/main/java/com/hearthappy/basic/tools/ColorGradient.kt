package com.hearthappy.basic.tools

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.DecelerateInterpolator

/**
 * 颜色渐变工具类：支持「单色渐变」和「双色渐变」自动切换
 * @param startColor 起始色（必填）
 * @param endColor 结束色（可选，不传则默认和startColor一致，启用单色模式）
 */
class ColorGradient(startColor: Int, endColor: Int? = null) {
    // 渐变模式：单色/双色
    private enum class GradientMode { SINGLE, DUAL }

    private val gradientMode: GradientMode

    // 单色模式下的唯一颜色（双色模式下无用）
    private var singleColor: Int

    // 双色模式下的两个颜色（单色模式下始终保持一致）
    var startColor: Int
    var endColor: Int

    private var colorAnimator: ValueAnimator? = null
    private var isColorAnimating = false // 避免动画叠加

    init { // 初始化模式和颜色：优先判断是否传入endColor
        this.startColor = startColor
        if (endColor != null && endColor != startColor) {
            this.endColor = endColor
            gradientMode = GradientMode.DUAL
            singleColor = startColor // 双色模式下单色变量仅作占位，不参与逻辑
        } else { // 未传endColor或两色相同 → 启用单色模式
            this.endColor = startColor
            singleColor = startColor
            gradientMode = GradientMode.SINGLE
        }
    }

    // =========================================================================================
    // 单色渐变
    // =========================================================================================
    /**
     * 单色渐变：从当前颜色过渡到目标颜色
     * @param targetColor 目标颜色（ARGB格式）
     * @param duration 渐变时长（默认600毫秒）
     * @param onUpdate 每帧颜色更新回调（返回当前渐变颜色，单色模式下两个参数相同，兼容原有回调格式）
     */
    fun animateToColor(targetColor: Int, duration: Long = 600L, onUpdate: (currentColor: Int) -> Unit) {
        if (isColorAnimating) {
            colorAnimator?.cancel()
        }

        // 获取当前单色的ARGB分量
        val currentA = Color.alpha(singleColor)
        val currentR = Color.red(singleColor)
        val currentG = Color.green(singleColor)
        val currentB = Color.blue(singleColor)

        // 目标颜色的ARGB分量
        val targetA = Color.alpha(targetColor)
        val targetR = Color.red(targetColor)
        val targetG = Color.green(targetColor)
        val targetB = Color.blue(targetColor)

        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float

                // 计算当前颜色（ARGB线性插值）
                val newA = currentA + (targetA - currentA) * fraction
                val newR = currentR + (targetR - currentR) * fraction
                val newG = currentG + (targetG - currentG) * fraction
                val newB = currentB + (targetB - currentB) * fraction
                singleColor = Color.argb(newA.toInt().coerceIn(0, 255), // 防止溢出（0-255范围）
                    newR.toInt().coerceIn(0, 255), newG.toInt().coerceIn(0, 255), newB.toInt().coerceIn(0, 255))

                // 单色模式下，startColor和endColor同步更新（兼容双色逻辑）
                startColor = singleColor // 回调返回当前颜色（两个参数相同，避免用户修改回调格式）
                onUpdate(singleColor)
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isColorAnimating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isColorAnimating = false
                    colorAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    isColorAnimating = false
                    colorAnimator = null
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    // =========================================================================================
    // 原有：双色渐变方法（兼容优化，单色模式下调用会自动同步两色）
    // =========================================================================================
    /**
     * 双色渐变：从当前[startColor→endColor]过渡到目标[targetStartColor→targetEndColor]
     * @param targetStartColor 目标起始色
     * @param targetEndColor 目标结束色
     * @param duration 渐变时长（默认600毫秒）
     * @param onUpdate 每帧颜色更新回调（返回当前startColor和endColor）
     */
    fun animateToColors(targetStartColor: Int, targetEndColor: Int, duration: Long = 600L, onUpdate: (currentStartColor: Int, currentEndColor: Int) -> Unit) {
        if (isColorAnimating) {
            colorAnimator?.cancel()
        }

        // 单色模式下：强制两色同步渐变（避免出现双色差异）
        val finalTargetStart = if (gradientMode == GradientMode.SINGLE) targetStartColor else targetStartColor
        val finalTargetEnd = if (gradientMode == GradientMode.SINGLE) targetStartColor else targetEndColor

        // 当前颜色分量
        val currentStartA = Color.alpha(startColor)
        val currentStartR = Color.red(startColor)
        val currentStartG = Color.green(startColor)
        val currentStartB = Color.blue(startColor)

        val currentEndA = Color.alpha(endColor)
        val currentEndR = Color.red(endColor)
        val currentEndG = Color.green(endColor)
        val currentEndB = Color.blue(endColor)

        // 目标颜色分量
        val targetStartA = Color.alpha(finalTargetStart)
        val targetStartR = Color.red(finalTargetStart)
        val targetStartG = Color.green(finalTargetStart)
        val targetStartB = Color.blue(finalTargetStart)

        val targetEndA = Color.alpha(finalTargetEnd)
        val targetEndR = Color.red(finalTargetEnd)
        val targetEndG = Color.green(finalTargetEnd)
        val targetEndB = Color.blue(finalTargetEnd)

        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float

                // 更新startColor
                val newStartA = currentStartA + (targetStartA - currentStartA) * fraction
                val newStartR = currentStartR + (targetStartR - currentStartR) * fraction
                val newStartG = currentStartG + (targetStartG - currentStartG) * fraction
                val newStartB = currentStartB + (targetStartB - currentStartB) * fraction
                startColor = Color.argb(newStartA.toInt().coerceIn(0, 255), newStartR.toInt().coerceIn(0, 255), newStartG.toInt().coerceIn(0, 255), newStartB.toInt().coerceIn(0, 255))

                // 更新endColor
                val newEndA = currentEndA + (targetEndA - currentEndA) * fraction
                val newEndR = currentEndR + (targetEndR - currentEndR) * fraction
                val newEndG = currentEndG + (targetEndG - currentEndG) * fraction
                val newEndB = currentEndB + (targetEndB - currentEndB) * fraction
                endColor = Color.argb(newEndA.toInt().coerceIn(0, 255), newEndR.toInt().coerceIn(0, 255), newEndG.toInt().coerceIn(0, 255), newEndB.toInt().coerceIn(0, 255))

                // 单色模式下同步更新singleColor
                if (gradientMode == GradientMode.SINGLE) {
                    singleColor = startColor
                }

                onUpdate(startColor, endColor)
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isColorAnimating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isColorAnimating = false
                    colorAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    isColorAnimating = false
                    colorAnimator = null
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    /**
     * 取消当前渐变动画
     */
    fun cancelAnimation() {
        colorAnimator?.cancel()
        isColorAnimating = false
        colorAnimator = null
    }
}