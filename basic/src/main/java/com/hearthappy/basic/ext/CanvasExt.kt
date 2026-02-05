package com.hearthappy.basic.ext

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.withTranslation
import kotlin.collections.map
import kotlin.text.toInt
import androidx.core.graphics.withSave
import kotlin.collections.find
import kotlin.math.abs


fun Canvas.useWave(wavePath: Path, cx: Float, cy: Float, radius: Float, waveHeight: Float, globalOffset: Float,      // 传入累加的全局偏移量
    speedMultiplier: Float,   // 传入倍速：1.0 为原速，2.0 为双倍
    progress: Float, block: Canvas.(Path) -> Unit) {
    wavePath.reset()

    // 1. 水位 Y 轴
    val waterLevelY = cy + radius - (2 * radius * (progress / 100f))

    // 2. 波长
    val waveW = radius * 2

    // 3. 核心计算：根据倍速计算最终的水平位移
    // 使用 (globalOffset * speedMultiplier) % 1f 确保位移永远在 [0, 1] 之间循环
    val currentMoveFactor = (globalOffset * speedMultiplier) % 1f
    val startX = (cx - radius) - (currentMoveFactor * waveW)

    wavePath.moveTo(startX, waterLevelY)

    // 4. 连续绘制 3 个波长确保平滑衔接
    for (i in 0 until 3) {
        val segmentX = startX + i * waveW
        wavePath.quadTo(segmentX + waveW / 4, waterLevelY - waveHeight, segmentX + waveW / 2, waterLevelY)
        wavePath.quadTo(segmentX + waveW * 3 / 4, waterLevelY + waveHeight, segmentX + waveW, waterLevelY)
    }

    // 5. 闭合路径
    wavePath.lineTo(startX + 3 * waveW, cy + radius)
    wavePath.lineTo(startX, cy + radius)
    wavePath.close()

    block(wavePath)
}

fun Canvas.withLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint, block: Canvas.() -> Unit) {
    val saveLayer = saveLayer(left, top, right, bottom, paint)
    block()
    restoreToCount(saveLayer)
}

fun Canvas.withLayer(view: View, paint: Paint, block: Canvas.() -> Unit) {
    if (view.width == 0 || view.height == 0) return
    val saveLayer = saveLayer(0f, 0f, view.width.toFloat(), view.height.toFloat(), paint)
    block()
    restoreToCount(saveLayer)
}


/**
 * 基础单文本绘制：支持 Padding、自动换行、位置对齐
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Canvas.drawTextInRect(text: String, rect: RectF, hPadding: Float = 0f, vPadding: Float = 0f, paint: TextPaint, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL) {
    val rw = abs(rect.width())
    val rh = abs(rect.height())
    if (rw <= hPadding * 2 || rh <= vPadding * 2) return

    val availableWidth = (rw - hPadding * 2).toInt()

    // 只能在绘制时创建 Layout，因为它依赖当前宽度
    val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, availableWidth).setAlignment(alignment).setIncludePad(false).build()

    val drawX = rect.left + hPadding
    val drawY = rect.top + vPadding + (rh - vPadding * 2 - layout.height) / 2f

    withTranslation(drawX, drawY) {
        layout.draw(this)
    }
}

/**
 * 多文本垂直排列 (性能版)：
 * 通过两个数组分别传递文本和画笔，避免创建 List 或 TextItem 对象
 * @param texts 文本数组
 * @param paints 对应文本的画笔数组
 * @param spacing 文本之间的间距
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Canvas.drawMultiTextVertical(texts: Array<String>, paints: Array<TextPaint>, rect: RectF, hPadding: Float = 0f, vPadding: Float = 0f, spacing: Float = 0f, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL) {
    val rw = abs(rect.width())
    val rh = abs(rect.height())

    // 基础校验：文本不能为空，Rect 宽度必须大于 Padding，画笔数组不能为空
    if (texts.isEmpty() || paints.isEmpty() || rw <= hPadding * 2) return

    val availableWidth = (rw - hPadding * 2).toInt()

    // 1. 构建 Layout 并计算总高度
    val layouts = arrayOfNulls<StaticLayout>(texts.size)
    var totalTextHeight = 0f

    for (i in texts.indices) { // 核心逻辑：如果 paints 数量足够则对应取值，否则取第一个画笔
        val currentPaint = if (i < paints.size) paints[i] else paints[0]

        val layout = StaticLayout.Builder.obtain(texts[i], 0, texts[i].length, currentPaint, availableWidth).setAlignment(alignment).setIncludePad(false).build()

        layouts[i] = layout
        totalTextHeight += layout.height
        if (i < texts.size - 1) totalTextHeight += spacing
    }

    // 2. 计算起始位置
    val drawX = rect.left + hPadding // 垂直居中公式：起始点 = Rect顶部 + 顶部Padding + (剩余可用高度 - 文本总高) / 2
    val startY = rect.top + vPadding + (rh - vPadding * 2 - totalTextHeight) / 2f

    // 3. 绘制逻辑
    withTranslation(drawX, startY) {
        for (i in layouts.indices) {
            val layout = layouts[i] ?: continue
            layout.draw(this) // 绘制完一行后，将坐标系下移：当前行高 + 间距
            translate(0f, layout.height + spacing)
        }
    }
}

enum class IconGravity { LEFT, RIGHT }

data class TextIconConfig(
    val index: Int,           // 对应 texts 的索引
    val drawable: Drawable,    // 图标
    val gravity: IconGravity,  // 左右方位
    val padding: Float = 8f,   // 图标与文字的间距
)

@RequiresApi(Build.VERSION_CODES.M)
fun Canvas.drawMultiTextVertical(texts: Array<String>, paints: Array<TextPaint>, rect: RectF, hPadding: Float = 0f, vPadding: Float = 0f, spacing: Float = 0f, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL, icons: List<TextIconConfig> = emptyList()) {
    val rw = abs(rect.width())
    val rh = abs(rect.height())

    if (texts.isEmpty() || paints.isEmpty() || rw <= hPadding * 2) return

    val availableWidth = (rw - hPadding * 2)
    val layouts = arrayOfNulls<StaticLayout>(texts.size)
    var totalTextHeight = 0f

    // 1. 构建 Layout 并预计算高度
    for (i in texts.indices) {
        val currentPaint = if (i < paints.size) paints[i] else paints[0]

        // 查找当前行是否有图标
        val iconConfig = icons.find { it.index == i }
        val iconSize = if (iconConfig != null) currentPaint.textSize else 0f
        val iconSpace = if (iconConfig != null) iconSize + iconConfig.padding else 0f

        // 核心：如果有图标，文字可用宽度需要减少
        val textWidth = (availableWidth - iconSpace).toInt().coerceAtLeast(0)

        val layout = StaticLayout.Builder.obtain(texts[i], 0, texts[i].length, currentPaint, textWidth).setAlignment(alignment).setIncludePad(false).build()

        layouts[i] = layout
        totalTextHeight += layout.height
        if (i < texts.size - 1) totalTextHeight += spacing
    }

    // 2. 计算起始 Y 坐标 (垂直居中)
    var currentY = rect.top + vPadding + (rh - vPadding * 2 - totalTextHeight) / 2f

    // 3. 绘制逻辑
    for (i in layouts.indices) {
        val layout = layouts[i] ?: continue
        val currentPaint = if (i < paints.size) paints[i] else paints[0]
        val iconConfig = icons.find { it.index == i }

        withTranslation(rect.left + hPadding, currentY) { // 移动到当前行的起始横坐标 (基础左边界)

            if (iconConfig != null) {
                val iconSize = currentPaint.textSize.toInt()
                val iconTop = (layout.getLineBottom(0) - layout.getLineTop(0) - iconSize) / 2 // 图标相对第一行文字垂直居中

                if (iconConfig.gravity == IconGravity.LEFT) { // 图标在左：先画图标，再平移画文字
                    iconConfig.drawable.setBounds(0, iconTop, iconSize, iconTop + iconSize)
                    iconConfig.drawable.draw(this)

                    translate(iconSize + iconConfig.padding, 0f)
                    layout.draw(this)
                } else { // 图标在右：先画文字，在文字末尾画图标
                    layout.draw(this)

                    val iconX = layout.width + iconConfig.padding
                    iconConfig.drawable.setBounds(iconX.toInt(), iconTop, (iconX + iconSize).toInt(), iconTop + iconSize)
                    iconConfig.drawable.draw(this)
                }
            } else { // 无图标正常绘制
                layout.draw(this)
            }
        }

        currentY += layout.height + spacing
    }
}