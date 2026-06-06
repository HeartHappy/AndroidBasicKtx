package com.hearthappy.basic.ext

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Size

enum class AnchorLocation {
    CENTER, //居中对齐
    LEFT_CENTER, RIGHT_CENTER, TOP_CENTER, BOTTOM_CENTER, //四边中心对齐
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT //四个角对齐
}

enum class IndicatorScaleType {
    ORIGINAL,    // 默认：保持原图大小（如果原图比控件大，则缩小以适配）
    FIT_SCALE   // 拉伸：缩放至填满整个控件边界
}

/**
 * Matrix 扩展函数：计算适配图标的变换矩阵
 * @param limitSize 容器/控件的大小。如果为 null 且模式为 FIT_SCALE，将退化为 ORIGINAL 模式。
 */
fun Matrix.transformed(indicator: Bitmap, targetX: Float, targetY: Float, anchor: AnchorLocation, scaleType: IndicatorScaleType = IndicatorScaleType.ORIGINAL, manualScale: Float = 1.0f, rotation: Float = 0f, limitSize: Size? = null): Matrix {
    val w = indicator.width.toFloat()
    val h = indicator.height.toFloat()

    // --- 步骤 1: 计算安全极限缩放比例 (fitScale) ---
    var fitScale = Float.MAX_VALUE // 默认不限制缩放

    limitSize?.let { size -> // 使用临时矩阵计算旋转后的占位尺寸，不破坏当前矩阵状态
        val tempMatrix = Matrix()
        tempMatrix.postRotate(rotation)
        val rect = RectF(0f, 0f, w, h)
        tempMatrix.mapRect(rect)

        val rotatedW = rect.width()
        val rotatedH = rect.height()

        // 计算适配容器的比例
        if (rotatedW > 0 && rotatedH > 0) {
            fitScale = (size.width.toFloat() / rotatedW).coerceAtMost(size.height.toFloat() / rotatedH)
        }
    }

    // --- 步骤 2: 确定最终缩放比例 ---
    val baseScale = when (scaleType) {
        IndicatorScaleType.FIT_SCALE -> { // 如果要求适配容器但没传大小，强制回退到原图模式
            if (limitSize == null) 1.0f else fitScale
        }
        IndicatorScaleType.ORIGINAL -> 1.0f
    }

    // 最终比例：应用用户比例，但不能超过容器限制（即：保证不超出控件）
    val finalScale = (baseScale * manualScale).coerceAtMost(fitScale)

    // --- 步骤 3: 计算原始图片上的“锚点”坐标 ---
    val (anchorX, anchorY) = when (anchor) {
        AnchorLocation.CENTER -> w / 2f to h / 2f
        AnchorLocation.LEFT_CENTER -> 0f to h / 2f
        AnchorLocation.RIGHT_CENTER -> w to h / 2f
        AnchorLocation.TOP_CENTER -> w / 2f to 0f
        AnchorLocation.BOTTOM_CENTER -> w / 2f to h
        AnchorLocation.TOP_LEFT -> 0f to 0f
        AnchorLocation.TOP_RIGHT -> w to 0f
        AnchorLocation.BOTTOM_LEFT -> 0f to h
        AnchorLocation.BOTTOM_RIGHT -> w to h
    }

    // --- 步骤 4: 构建最终矩阵 ---
    this.reset() // A. 锚点归零
    this.postTranslate(-anchorX, -anchorY) // B. 缩放
    this.postScale(finalScale, finalScale) // C. 旋转
    this.postRotate(rotation) // D. 平移到目标点
    this.postTranslate(targetX, targetY)

    return this
}