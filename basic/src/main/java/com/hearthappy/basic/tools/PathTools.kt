package com.hearthappy.basic.tools

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.graphics.RectF
import android.util.Size
import android.view.View
import kotlin.math.hypot

/**
 * Created Date: 2025/9/25/周四
 * @author ChenRui
 * ClassDescription：路径处理工具
 */
object PathTools {
    /**
     * 获取 Path 上的所有点坐标（等间距取样）
     * @param path 原始路径
     * @param step 取样步长（建议 1px）
     * @return 路径上的点集合
     */
    fun getPointsOnPath(path: Path?, step: Float): List<PointF> {
        val points: MutableList<PointF> = kotlin.collections.ArrayList()
        val measure = PathMeasure(path, false)
        val pathLength = measure.length

        var distance = 0f
        val pos = FloatArray(2) // 存储坐标 [x, y]


        // 等间距遍历路径
        while (distance <= pathLength) {
            measure.getPosTan(distance, pos, null) // 获取指定距离处的坐标
            points.add(PointF(pos[0], pos[1]))
            distance += step
        }


        // 确保终点被包含（处理浮点精度问题）
        if (pathLength > 0 && distance - step < pathLength) {
            measure.getPosTan(pathLength, pos, null)
            points.add(PointF(pos[0], pos[1]))
        }

        return points
    }

    /**
     * 获取 Path 上的所有点坐标（精确包含起点终点）
     * @param path 原始路径
     * @return 路径上的点集合（包括起点和终点）
     */
    fun getPointsOnPath(path: Path?): List<PointF> {
        return getPointsOnPath(path, 1f) // 默认 1px 步长
    }

    /**
     * Get point percentage on path
     * 计算目标点在路径上的百分比
     *
     * @param point x,y坐标点
     * @param pathMeasure
     * @return 百分比（0~1f），-1f表示未找到
     */
    fun getPointPercentageOnPath(point: FloatArray, pathMeasure: PathMeasure): Float {
        val pathLength = pathMeasure.length
        if (pathLength <= 0) return -1f

        // 二分法查找（效率更高）
        var start = 0f
        var end = pathLength
        val pos = FloatArray(2) // 路径上的点
        val threshold = 1f // 精度阈值（像素）

        // 最多迭代20次（足够达到高精度）
        kotlin.repeat(20) {
            val mid = (start + end) / 2f
            pathMeasure.getPosTan(mid, pos, null)

            // 计算与目标点的距离
            val dx = pos[0] - point[0]
            val dy = pos[1] - point[1]
            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()

            if (distance < threshold) { // 找到足够接近的点，返回百分比
                return mid / pathLength
            }

            // 调整二分范围（需要结合路径方向判断，这里简化处理）
            // 更精确的实现需要判断点在路径的哪一侧
            if (mid < pathLength / 2) {
                start = mid
            } else {
                end = mid
            }
        }

        // 如果循环结束仍未找到，返回最接近的结果
        val mid = (start + end) / 2f
        return mid / pathLength
    }

    /**
     * 获取路径的起点和终点坐标
     * @param path 目标路径
     * @return 长度为2的数组，[0]是起点坐标（floatArrayOf(x, y)），[1]是终点坐标（floatArrayOf(x, y)）
     *         若路径为空或长度为0，返回null
     */
    fun getPathBothEndsPoints(path: Path): Array<FloatArray>? {
        val pathMeasure = PathMeasure(path, false) // false：不闭合路径（确保终点不与起点重合）
        val pathLength = pathMeasure.length

        // 路径为空或长度为0，返回null
        if (pathLength <= 0) {
            return null
        }

        // 存储起点和终点坐标（每个点用float[2]表示，x=float[0], y=float[1]）
        val startPoint = FloatArray(2)
        val endPoint = FloatArray(2)

        // 获取起点（距离0处的坐标）
        pathMeasure.getPosTan(0f, startPoint, null)

        // 获取终点（距离=路径总长度处的坐标）
        pathMeasure.getPosTan(pathLength, endPoint, null)

        return arrayOf(startPoint, endPoint)
    }

    /**
     * 通过 PathMeasure 实现路径水平镜像
     * @param originalPath 原路径
     * @param view 目标控件（用于获取中心X坐标）
     * @param step 取点步长（单位：px，值越小镜像越精确，建议设为1）
     * @return 水平镜像后的新路径
     */
    fun horizontalMirror(originalPath: Path, view: View, step: Float = 1f): Path {
        val mirrorPath = Path()
        val pathMeasure = PathMeasure(originalPath, false) // 不闭合路径
        val pathLength = pathMeasure.length

        // 1. 获取控件水平镜像轴（中心X坐标）
        val centerX = view.width / 2f

        // 2. 存储当前点和上一个点的坐标（用于连接镜像点）
        val currentPoint = FloatArray(2)
        var lastMirrorPoint: FloatArray? = null

        // 3. 以固定步长遍历原路径的所有点
        var distance = 0f // 当前遍历到的路径距离
        while (distance <= pathLength) { // 获取原路径上当前距离的点坐标
            pathMeasure.getPosTan(distance, currentPoint, null)

            // 4. 计算该点的水平镜像坐标（y不变，x关于centerX对称）
            val mirrorX = 2 * centerX - currentPoint[0]
            val mirrorY = currentPoint[1]
            val mirrorPoint = floatArrayOf(mirrorX, mirrorY)

            // 5. 重建镜像路径：第一个点用moveTo，后续点用lineTo连接
            if (lastMirrorPoint == null) {
                mirrorPath.moveTo(mirrorPoint[0], mirrorPoint[1]) // 起点
            } else {
                mirrorPath.lineTo(mirrorPoint[0], mirrorPoint[1]) // 连接到下一个镜像点
            }

            // 更新距离和上一个镜像点
            distance += step
            lastMirrorPoint = mirrorPoint
        }

        // 6. 处理原路径的闭合状态（如果原路径是闭合的，镜像路径也需要闭合）
        if (pathMeasure.isClosed) {
            mirrorPath.close()
        }

        return mirrorPath
    }

    /**
     * Scale path
     * 缩放路径到控件大小
     *
     * @param path 原始路径
     * @param viewSize 控件尺寸
     * @return 缩放比例
     */
    fun getPathScaleSize(path: Path, viewSize: Size): Float {
        val originalBounds = RectF()
        path.computeBounds(originalBounds, true)
        val scaleX = viewSize.width.toFloat() / originalBounds.width()  // 按宽度缩放的比例
        val scaleY = viewSize.height.toFloat() / originalBounds.height() // 按高度缩放的比例
        val scale = kotlin.comparisons.minOf(scaleX, scaleY)     // 取较小的比例，保证路径完全显示
        return scale
    }

    /**
     * 将 Path 缩放并居中到目标矩形区域内
     * @param sourcePath 原始路径
     * @param targetRect 目标区域（通常是 View 的内边距区域）
     * @param strokeWidth 描边宽度（可选，如果 Path 是 Stroked，需要预留空间防止溢出）
     * @return 变换后的 Path
     */
    fun fitCenter(sourcePath: Path, targetRect: RectF, strokeWidth: Float = 0f): Path {
        val resultPath = Path(sourcePath)
        val pathBounds = RectF()

        // 1. 计算 Path 的原始边界
        sourcePath.computeBounds(pathBounds, true)

        // 2. 考虑描边宽度，收缩目标区域
        val inset = strokeWidth / 2f
        val safeTarget = RectF(targetRect).apply {
            if (inset > 0) inset(inset, inset)
        }

        val matrix = Matrix()

        // 3. 计算缩放比例：取宽高缩放比的最小值，确保 Path 能够完全包含在内
        val scaleX = safeTarget.width() / pathBounds.width()
        val scaleY = safeTarget.height() / pathBounds.height()
        val scale = minOf(scaleX, scaleY)

        // 4. 构建变换矩阵
        // a. 将 Path 的几何中心移动到 (0,0)
        matrix.postTranslate(-pathBounds.centerX(), -pathBounds.centerY())
        // b. 执行等比缩放
        matrix.postScale(scale, scale)
        // c. 移动到目标区域的中心
        matrix.postTranslate(safeTarget.centerX(), safeTarget.centerY())

        // 5. 应用变换
        resultPath.transform(matrix)

        return resultPath
    }
}