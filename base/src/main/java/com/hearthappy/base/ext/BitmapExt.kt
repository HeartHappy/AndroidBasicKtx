package com.hearthappy.base.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt


/**
 * 高斯模糊
 * @receiver Bitmap
 * @param context Context
 * @param radius Float
 * @return Bitmap
 */
fun Bitmap.blur(context: Context, radius: Float): Bitmap {
    val width = (this.width * 0.4).roundToInt()
    val height = (this.height * 0.4).roundToInt()

    val inputBitmap = Bitmap.createScaledBitmap(this, width, height, false)
    val outputBitmap = Bitmap.createBitmap(inputBitmap)

    val rs = RenderScript.create(context)
    val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
    val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
    theIntrinsic.setRadius(radius)
    theIntrinsic.setInput(tmpIn)
    theIntrinsic.forEach(tmpOut)
    tmpOut.copyTo(outputBitmap)
    return outputBitmap
}

/**
 * 颜色反转.例如黑色背景bitmap，转换成白色bitmap
 * @receiver Bitmap
 * @return Bitmap
 */
fun Bitmap.colorReversal(): Bitmap {
    // 创建一个颜色矩阵，用于反转颜色
    val colorMatrix = ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,  // 红色通道反转
            0f, -1f, 0f, 0f, 255f,  // 绿色通道反转
            0f, 0f, -1f, 0f, 255f,  // 蓝色通道反转
            0f, 0f, 0f, 1f, 0f // 保持透明度不变
        )
    )

    // 创建一个颜色过滤器，使用颜色矩阵
    val colorFilter: ColorFilter = ColorMatrixColorFilter(colorMatrix)

    // 创建一个新的Bitmap，用于存储处理后的图像
    val invertedBitmap = Bitmap.createBitmap(this.width, this.height, this.config)

    // 创建一个Canvas对象
    val canvas = Canvas(invertedBitmap)

    // 创建一个Paint对象，并设置颜色过滤器
    val paint = Paint()
    paint.colorFilter = colorFilter

    // 在Canvas上绘制原始Bitmap，并应用颜色过滤器
    canvas.drawBitmap(this, 0f, 0f, paint)
    return invertedBitmap
}


fun Bitmap.toBase64(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}
