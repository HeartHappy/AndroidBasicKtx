package com.hearthappy.basic.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File


fun Context.fileToUri(file: File): Uri {
    //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

/**
 * 分享图片到其他app
 * @receiver Context
 * @param file File
 * @param title String
 */
fun Context.shareImage(file: File, title: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/*"
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.putExtra(Intent.EXTRA_STREAM, fileToUri(file))
    startActivity(Intent.createChooser(intent, title))
}

/**
 * 分享图片列表到其他app
 * @receiver Context
 * @param files List<File>
 * @param title String
 */
fun Context.shareImages(uris: ArrayList<Uri>, title: String) {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
    intent.type = "image/*"
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
    startActivity(Intent.createChooser(intent, title))
}

