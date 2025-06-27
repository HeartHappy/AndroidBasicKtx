package com.hearthappy.basic.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun ImageView.testLoadUrl(urlString: String) {
    Thread {
        var bitmap: Bitmap? = null
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        post { bitmap?.let { setImageBitmap(it) } }
    }.start()
}