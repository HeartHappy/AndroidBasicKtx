package com.hearthappy.basic.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager


class VerticalViewPager : ViewPager {
    private var isSwipeEnabled = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() { // The majority of the magic happens here
        setPageTransformer(true, VerticalPageTransformer()) // The easiest way to get rid of the overscroll drawing that happens on the left and right
        overScrollMode = OVER_SCROLL_NEVER
    }

    private inner class VerticalPageTransformer : PageTransformer {
        override fun transformPage(view: View, position: Float) {
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.alpha = 0f
            } else if (position <= 1) { // [-1,1]
                view.alpha = 1f

                // Counteract the default slide transition
                view.translationX = view.width * -position

                //set Y position to swipe in from top
                val yPosition = position * view.height
                view.translationY = yPosition
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.alpha = 0f
            }
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return false
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean { //        val intercepted = super.onInterceptTouchEvent(swapXY(ev))
        //        swapXY(ev) // return touch coordinates to original reference frame for any child views
        return isSwipeEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isSwipeEnabled && super.onTouchEvent(ev)
    }

    fun setSwipeEnabled(enabled: Boolean) {
        this.isSwipeEnabled = enabled
    }

    val rect = Rect()
    var carouselIndex = 0
    var delay = 3000L
    fun setCarouse(isCarouse: Boolean) {
        if (isCarouse) {
            setSwipeEnabled(true)
            postDelayed(carouseTask, delay)
        }
    }

    private val carouseTask = object : Runnable {
        override fun run() {
            if (getGlobalVisibleRect(rect)) {
                ++carouselIndex
                if (carouselIndex >= childCount) {
                    carouselIndex = 0
                }
                setCurrentItem(carouselIndex, carouselIndex != 0)
            }
            postDelayed(this, delay)
        }
    }
    fun startCarouse() {
        setCarouse(true)
    }

    fun stopCarouse() {
        removeCallbacks(carouseTask)
    }
}