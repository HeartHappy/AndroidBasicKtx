package com.hearthappy.basic.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

fun String.substringMiddle(prefix: String, suffix: String, jumpOverCount: Int = 0, ignoreCount: Int = 0, missingDelimiterValue: String = this): String {
    val prefixIndex = indexOf(prefix)
    val delPrefixBefore = if (prefixIndex == -1) missingDelimiterValue else substring(prefixIndex + jumpOverCount, length)
    val suffixIndex = delPrefixBefore.indexOf(suffix)

    return if (suffixIndex == -1) delPrefixBefore else delPrefixBefore.substring(0, suffixIndex + suffix.length - ignoreCount)
}

/**
 * base64 字符串转 Bitmap
 * @receiver String
 * @return Bitmap
 */
fun String.fromBase64ToBitmap(): Bitmap {
    val bytes = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}


/**
 * base64 转 byteArray
 * @receiver String
 * @return ByteArray
 */
fun String.fromBase64ToByteArray(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}

/**
 * 图片文件转换成base64
 * @receiver String 文件路径
 * @return String 文件base64
 */
fun String.fromFile2Base64(): String {
    val bitmap = BitmapFactory.decodeFile(this)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}