package com.hearthappy.base.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import java.lang.ref.WeakReference

class CenteredImageSpan : ImageSpan {
    private var mDrawableRef: WeakReference<Drawable?>? = null

    constructor(drawable: Drawable?) : super(drawable!!, ALIGN_BOTTOM)

    constructor(context: Context?, bm: Bitmap?) : super(context!!, bm!!, ALIGN_BOTTOM)

    constructor(context: Context?, @DrawableRes resourceId: Int, verticalAlignment: Int) : super(context!!, resourceId, verticalAlignment)

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: FontMetricsInt?): Int {
        val d = cachedDrawable
        val rect = d!!.bounds

        if (fm != null) {
            val pfm = paint.fontMetricsInt // keep it the same as paint's fm
            fm.ascent = pfm.ascent
            fm.descent = pfm.descent
            fm.top = pfm.top
            fm.bottom = pfm.bottom
        }

        return rect.right
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val b = cachedDrawable
        canvas.save()
        val transY = top + (bottom - top - b!!.bounds.bottom) / 2

        //        int transY = bottom - b.getBounds().bottom // align bottom to bottom // at least , this work
        //                - Math.abs(bottom - b.getBounds().bottom ) / 2;  // align center to center
        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

    private val cachedDrawable: Drawable?
        // Redefined locally because it is a private member from DynamicDrawableSpan
        get() {
            val wr = mDrawableRef
            var d: Drawable? = null

            if (wr != null) {
                d = wr.get()
            }

            if (d == null) {
                d = drawable
                mDrawableRef = WeakReference(d)
            }

            return d
        }
}