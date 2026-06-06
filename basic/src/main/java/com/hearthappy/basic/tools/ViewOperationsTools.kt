package com.hearthappy.basic.tools

import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Created Date: 2025/11/5/周三
 * @author ChenRui
 * ClassDescription：视图操作工具类
 * 一、点击事件中的点的处理
 *  1、支持点在矩形内
 *  2、支持点在多边形内
 *  3、支持点在圆内
 *  4、支持点在线段上
 *  5、支持点在弧线上
 *  6、获取两条直线的相交点
 *
 */
object ViewOperationsTools {


    /**
     * 1. 判断点击位置是否在矩形内（支持两种矩形定义方式）
     * @param touchX 点击X坐标（单位需统一，如像素）
     * @param touchY 点击Y坐标
     * @param rectLeft 矩形左边界X
     * @param rectTop 矩形上边界Y
     * @param rectRight 矩形右边界X
     * @param rectBottom 矩形下边界Y
     * @param includeBoundary 是否包含矩形边界（默认true）
     * @return true：在矩形内；false：不在
     */
    fun isClickInRect(touchX: Float, touchY: Float, rectLeft: Float, rectTop: Float, rectRight: Float, rectBottom: Float, includeBoundary: Boolean = true): Boolean { // 确保left <= right，top <= bottom（避免传入参数顺序错误）
        val left = minOf(rectLeft, rectRight)
        val right = maxOf(rectLeft, rectRight)
        val top = minOf(rectTop, rectBottom)
        val bottom = maxOf(rectTop, rectBottom)

        return if (includeBoundary) {
            touchX >= left && touchX <= right && touchY >= top && touchY <= bottom
        } else {
            touchX > left && touchX < right && touchY > top && touchY < bottom
        }
    }

    fun isClickInRect(touchX: Float, touchY: Float, rect: RectF, includeBoundary: Boolean = true): Boolean {
        return isClickInRect(touchX, touchY, rect.left, rect.top, rect.right, rect.bottom, includeBoundary)
    }

    /**
     * 重载：通过矩形中心+宽高判断（更直观）
     * @param rectCenter 矩形中心坐标
     * @param rectWidth 矩形宽度
     * @param rectHeight 矩形高度
     */
    fun isClickInRect(touchX: Float, touchY: Float, rectCenter: PointF, rectWidth: Float, rectHeight: Float, includeBoundary: Boolean = true): Boolean {
        val rectLeft = rectCenter.x - rectWidth / 2f
        val rectTop = rectCenter.y - rectHeight / 2f
        val rectRight = rectCenter.x + rectWidth / 2f
        val rectBottom = rectCenter.y + rectHeight / 2f
        return isClickInRect(touchX, touchY, rectLeft, rectTop, rectRight, rectBottom, includeBoundary)
    }

    /**
     * 2. 判断点击位置是否在多边形内（射线法，支持任意凸/凹多边形）
     * @param touchX 点击X坐标
     * @param touchY 点击Y坐标
     * @param polygonPoints 多边形顶点列表（按顺时针/逆时针顺序排列，无需闭合）
     * @param includeBoundary 是否包含多边形边界（默认true）
     * @return true：在多边形内；false：不在
     */
    fun isClickInPolygon(touchX: Float, touchY: Float, polygonPoints: List<PointF>, includeBoundary: Boolean = true): Boolean {
        if (polygonPoints.size < 3) return false // 多边形至少3个顶点

        var isInside = false
        val pointCount = polygonPoints.size

        for (i in 0 until pointCount) {
            val p1 = polygonPoints[i]
            val p2 = polygonPoints[(i + 1) % pointCount] // 最后一个点连接第一个点

            // 先判断是否在边界上（包含边界时）
            if (includeBoundary && isClickOnLineSegment(touchX, touchY, p1.x, p1.y, p2.x, p2.y)) {
                return true
            }

            // 射线法核心逻辑：判断射线（向右水平）与边的交点
            if ((p1.y > touchY) != (p2.y > touchY)) { // 边跨越射线Y坐标
                // 计算交点X坐标
                val xIntersect = (touchY - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x
                if (touchX < xIntersect) { // 交点在射线右侧
                    isInside = !isInside
                }
            }
        }
        return isInside
    }

    /**
     * 3. 判断点击位置是否在圆内
     * @param touchX 点击X坐标
     * @param touchY 点击Y坐标
     * @param circleCenter 圆心坐标
     * @param circleRadius 圆半径（单位需与坐标统一）
     * @param includeBoundary 是否包含圆边界（默认true）
     * @return true：在圆内；false：不在
     */
    fun isClickInCircle(touchX: Float, touchY: Float, circleCenter: PointF, circleRadius: Float, includeBoundary: Boolean = true): Boolean {
        if (circleRadius <= 0) return false // 半径无效

        // 计算点击点到圆心的距离平方（避免开根号，提升性能）
        val dx = touchX - circleCenter.x
        val dy = touchY - circleCenter.y
        val distanceSquare = dx * dx + dy * dy
        val radiusSquare = circleRadius * circleRadius

        return if (includeBoundary) {
            distanceSquare <= radiusSquare + 1e-6f // 加微小值避免浮点精度问题
        } else {
            distanceSquare < radiusSquare - 1e-6f
        }
    }

    /**
     * 4. 判断点击位置是否在线段上（支持有宽度的线段，非纯数学直线）
     * @param touchX 点击X坐标
     * @param touchY 点击Y坐标
     * @param lineStartX 线段起点X
     * @param lineStartY 线段起点Y
     * @param lineEndX 线段终点X
     * @param lineEndY 线段终点Y
     * @param lineWidth 线段宽度（默认2f，单位需与坐标统一）
     * @return true：在线段上；false：不在
     */
    fun isClickOnLineSegment(touchX: Float, touchY: Float, lineStartX: Float, lineStartY: Float, lineEndX: Float, lineEndY: Float, lineWidth: Float = 2f): Boolean {
        if (lineWidth <= 0) return false

        // 步骤1：计算点击点到线段的垂直距离（判断是否在宽度范围内）
        val distanceToLine = pointToLineDistance(touchX, touchY, lineStartX, lineStartY, lineEndX, lineEndY)
        if (distanceToLine > lineWidth / 2f + 1e-6f) {
            return false // 超出线段宽度范围
        }

        // 步骤2：判断点击点是否在线段的两个端点之间（避免命中延长线）
        val dotProduct = (touchX - lineStartX) * (lineEndX - lineStartX) + (touchY - lineStartY) * (lineEndY - lineStartY)
        if (dotProduct < -1e-6f) { // 点击点在起点外侧
            return false
        }

        val lineLengthSquare = (lineEndX - lineStartX) * (lineEndX - lineStartX) + (lineEndY - lineStartY) * (lineEndY - lineStartY)
        if (dotProduct > lineLengthSquare + 1e-6f) { // 点击点在终点外侧
            return false
        }

        return true
    }


    /**
     * 判断触摸点是否在指定弧线上（优化：扩大点击区域）
     * @param touchX 触摸点X坐标（已处理旋转偏移）
     * @param touchY 触摸点Y坐标（已处理旋转偏移）
     * @param rect 弧线所在椭圆的矩形
     * @param startAngle 弧线起始角度（度）
     * @param sweepAngle 弧线扫描角度（度）
     * @param strokeWidth 弧线画笔宽度（视觉宽度）
     * @param expandScope 弧线点击区域扩展范围（视觉宽度）:默认扩大20px
     * @return 是否命中弧线（含扩大的点击区域）
     */
    fun isClickOnArc(touchX: Float, touchY: Float, rect: RectF, startAngle: Float, sweepAngle: Float, strokeWidth: Float, expandScope: Float = 20f): Boolean {
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val radiusX = rect.width() / 2f
        val radiusY = rect.height() / 2f
        if (radiusX <= 0 || radiusY <= 0) return false

        val x = touchX - centerX
        val y = touchY - centerY

        val normalizedX = x / radiusX
        val normalizedY = y / radiusY

        val distance = kotlin.math.sqrt(normalizedX * normalizedX + normalizedY * normalizedY)

        val visualTolerance = (strokeWidth / 2f) / maxOf(radiusX, radiusY)
        val extraTolerance = expandScope / maxOf(radiusX, radiusY)
        val totalTolerance = visualTolerance + extraTolerance

        if (distance < (1 - totalTolerance) || distance > (1 + totalTolerance)) {
            return false
        }

        var angleDeg = toDegrees(atan2(normalizedY.toDouble(), normalizedX.toDouble())).toFloat()
        angleDeg = (angleDeg + 360) % 360

        val endAngle = startAngle + sweepAngle
        val start = (startAngle + 360) % 360
        val end = (endAngle + 360) % 360

        val isInAngleRange = if (start <= end) {
            angleDeg in start..end
        } else {
            angleDeg >= start || angleDeg <= end
        }

        return isInAngleRange
    }

    /**
     * 判断convertRect是否完全在region内（均为RectF类型）
     * @param convertRect 待判断的矩形
     * @param region 目标区域（矩形）
     * @return true：convertRect完全在region内；false：否则
     */
    fun isRectFullyInRect(convertRect: RectF, region: RectF): Boolean { // 处理空矩形情况（宽或高为0的矩形视为“空”）
        if (convertRect.isEmpty || region.isEmpty) {
            return false
        }

        // 检查四个边界是否都在region内
        val isLeftIn = convertRect.left >= region.left
        val isTopIn = convertRect.top >= region.top
        val isRightIn = convertRect.right <= region.right
        val isBottomIn = convertRect.bottom <= region.bottom

        // 四个条件必须同时满足
        return isLeftIn && isTopIn && isRightIn && isBottomIn
    }

    /**
     * 获取两条直线的交点
     *
     * @param sp1 线段1的起始点
     * @param ep1 线段1的结束点
     * @param sp2 线段2的起始点
     * @param ep2 线段2的结束点
     * @return 交点，如果不存在则返回null
     */
    fun getIntersectionPoint(sp1: Point, ep1: Point, sp2: Point, ep2: Point): Point? { // 计算直线的斜率和截距
        val a1 = ep1.y - sp1.y
        val b1 = sp1.x - ep1.x
        val c1 = a1 * sp1.x + b1 * sp1.y

        val a2 = ep2.y - sp2.y
        val b2 = sp2.x - ep2.x
        val c2 = a2 * sp2.x + b2 * sp2.y

        val determinant = a1 * b2 - a2 * b1

        if (determinant == 0) { // 两条直线平行或重合
            return null
        } else { // 计算交点坐标
            val x = (b2 * c1 - b1 * c2) / determinant
            val y = (a1 * c2 - a2 * c1) / determinant
            return Point(x, y)
        }
    }



    /**
     * 辅助方法：计算点到线段的垂直距离（数学公式）
     */
    private fun pointToLineDistance(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float { // 向量AB
        val abX = x2 - x1
        val abY = y2 - y1 // 向量AP
        val apX = px - x1
        val apY = py - y1

        // 点积：AP · AB
        val dotProduct = apX * abX + apY * abY
        if (dotProduct <= 0) { // 点在A点外侧，距离=AP长度
            return hypot(apX, apY)
        }

        // 线段长度平方
        val abLengthSquare = abX * abX + abY * abY
        if (dotProduct >= abLengthSquare) { // 点在B点外侧，距离=BP长度
            return hypot(px - x2, py - y2)
        }

        // 点在线段中间，距离=三角形面积×2 / 线段长度
        val crossProduct = apX * abY - apY * abX
        return abs(crossProduct) / sqrt(abLengthSquare)
    }
}