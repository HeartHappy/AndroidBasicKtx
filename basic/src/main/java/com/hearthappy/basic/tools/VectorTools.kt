package com.hearthappy.basic.tools

import android.graphics.Point
import android.graphics.PointF
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 向量工具集
 *
 * @constructor Create empty Vector tools
 */
object VectorTools {
    /**
     * 计算两个向量的夹角（度）
     */
    fun angleBetween(vec1: PointF, vec2: PointF): Double { // 计算点积
        val dotProduct = vec1.x * vec2.x + vec1.y * vec2.y // 计算两个向量的模长
        val length1 = hypot(vec1.x.toDouble(), vec1.y.toDouble())
        val length2 = hypot(vec2.x.toDouble(), vec2.y.toDouble()) // 防止除以零
        if (length1 == 0.0 || length2 == 0.0) return 0.0 // 计算余弦值（限制范围避免精度问题导致acos返回NaN）
        val cosine = (dotProduct / (length1 * length2)).coerceIn(-1.0, 1.0) // 转换为角度
        return toDegrees(acos(cosine))
    }

    /**
     * 按顺时针方向获取与给定向量成指定夹角的新向量
     *
     * @param v
     * @param angle
     * @return
     */
    fun getVectorByAngle(v: PointF, angle: Double): PointF { // 计算原向量与X轴正方向的夹角（弧度）
        val originalAngle = atan2(v.y.toDouble(), v.x.toDouble()) // 加上顺时针旋转的角度（转为弧度），顺时针旋转角度为负
        val newAngleRadians = originalAngle + toRadians(angle) // 原向量的模长
        val length = hypot(v.x.toDouble(), v.y.toDouble()) // 计算新向量的分量
        val x = (cos(newAngleRadians) * length).toFloat()
        val y = (sin(newAngleRadians) * length).toFloat()
        return PointF(x, y)
    }


    /**
     * 判断一个点在不在多边形内
     */
    /**
     * 判断点是否在多边形内
     * @param point 检测点
     * @param pts   多边形的顶点
     * @return      点在多边形内返回true,否则返回false
     */
    fun isPointInPoly(point: Point, pts: List<Point>): Boolean {
        val n = pts.size
        val boundOrVertex = true //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        var intersectCount = 0 //cross points count of x
        val precision = 2e-10 //浮点类型计算时候与0比较时候的容差
        var p1: Point
        var p2: Point //neighbour bound vertices
        val p: Point = point //当前点
        p1 = pts[0] //left vertex
        for (i in 1..n) { //check all rays
            if (p == p1) {
                return boundOrVertex //p is an vertex
            }
            p2 = pts[i % n] //right vertex
            if (p.x < min(p1.x, p2.x) || p.x > max(p1.x, p2.x)) { //ray is outside of our interests
                p1 = p2
                continue  //next ray left point
            }
            if (p.x > min(p1.x, p2.x) && p.x < max(p1.x, p2.x)) { //ray is crossing over by the algorithm (common part of)
                if (p.y <= max(p1.y, p2.y)) { //x is before of ray
                    if (p1.x == p2.x && p.y >= p1.y.coerceAtMost(p2.y)) { //overlies on a horizontal ray
                        return boundOrVertex
                    }
                    if (p1.y == p2.y) { //ray is vertical
                        if (p1.y == p.y) { //overlies on a vertical ray
                            return boundOrVertex
                        } else { //before ray
                            ++intersectCount
                        }
                    } else { //cross point on the left side
                        val xinters: Int = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y //cross point of y
                        if (abs(p.y - xinters) < precision) { //overlies on a ray
                            return boundOrVertex
                        }
                        if (p.y < xinters) { //before ray
                            ++intersectCount
                        }
                    }
                }
            } else { //special case when ray is crossing through the vertex
                if (p.x == p2.x && p.y <= p2.y) { //p crossing over p2
                    val p3: Point = pts[(i + 1) % n] //next vertex
                    if (p.x >= p1.x.coerceAtMost(p3.x) && p.x <= p1.x.coerceAtLeast(p3.x)) { //p.x lies between p1.x & p3.x
                        ++intersectCount
                    } else {
                        intersectCount += 2
                    }
                }
            }
            p1 = p2 //next ray left point
        }
        return intersectCount % 2 != 0
    }


    /**
     * 已知三角形的三边，求边 a 正对的角度 大小，返回是弧度值
     */
    fun getAngle(a: Float, b: Float, c: Float): Double {
        return toDegrees(acos((b * b + c * c - a * a) / (2 * b * c)).toDouble())
    }

    /**
     * 已知三角形的一边和该边相邻的两个角，求角 a 正对的边的长度,返回是长度
     * a 夹角角度  ,角度
     * b 边长
     * c 夹角角度
     * 返回 a 角对的边长
     */
    fun getEdge(a: Float, b: Float, c: Float): Double {
        return toDegrees(acos((b * b + c * c - a * a) / (2 * b * c)).toDouble())
    }

    /**
     * 已知对角a 和 a相邻的 边ea和eb，求角a对应的边长
     */
    fun getEdgeByAngleAndEdge(a: Float, ea: Float, eb: Float): Float {
        return sqrt(ea * ea + eb * eb - cos(toRadians(a.toDouble())) * 2 * ea * eb).toFloat()
    }


}