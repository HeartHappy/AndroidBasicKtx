package com.hearthappy.basic.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.ext.scrollAnimator

/**
 * Created Date: 2025/4/27
 * @author ChenRui
 * ClassDescription：基于ViewPager2封装的无限轮播控件
 */
class CarouselView : FrameLayout {
    private val viewPager2 = ViewPager2(context)
    private var isViewVisible = false
    private var interval: Long = 3000
    private var duration: Long = 1000

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        isViewVisible = visibility == VISIBLE
    }

    init {
        addView(viewPager2)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        viewPager2.adapter = adapter //轮播图滑动监听
        viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    fun getAdapter(): RecyclerView.Adapter<*>? {
        return viewPager2.adapter
    }

    fun setOrientation(orientation: Int) {
        viewPager2.orientation = orientation
    }

    fun getOrientation(): Int {
        return viewPager2.orientation
    }

    fun setPageTransformer(transformer: ViewPager2.PageTransformer) {
        viewPager2.setPageTransformer(transformer)
    }

    fun isUserInputEnabled(enabled: Boolean) {
        viewPager2.isUserInputEnabled = enabled
    }

    fun addListener(onPageSelected: (Int) -> Unit, onPageScrolled: (Int, Float, Int) -> Unit = { p, po, pop -> }, onPageScrollStateChanged: (Int) -> Unit = {}) {
        val absSpecialAdapter = viewPager2.adapter as AbsSpecialAdapter<*, *>?
        val itemRealCount = absSpecialAdapter?.getItemRealCount() ?: -1
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onPageSelected(itemRealCount.takeIf { it == -1 }?.run { position } ?: (position % itemRealCount))
            }

            override fun onPageScrollStateChanged(state: Int) {
                onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                onPageScrolled(itemRealCount.takeIf { it == -1 }?.run { position } ?: (position % itemRealCount), positionOffset, positionOffsetPixels)
            }
        })
    }

    fun setScrollInterval(interval: Long, duration: Long = 1000) {
        this.interval = interval
        this.duration = duration
    }

    private fun startAutoScroll() {
        postDelayed(carouselTask, interval)
    }

    private val carouselTask = object : Runnable {
        override fun run() {
            if (isViewVisible) {
                var currentItem = viewPager2.currentItem
                if (++currentItem >= Int.MAX_VALUE) currentItem = 0
                viewPager2.scrollAnimator(currentItem, duration)
            }
            postDelayed(this, interval)
        }

    }

    private fun stopAutoScroll() {
        removeCallbacks(carouselTask)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAutoScroll()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoScroll()
    }

    abstract class AbsCarouselAdapter<VB : ViewBinding, T> : AbsSpecialAdapter<VB, T>() {

        override fun getItemCount(): Int {
            return Int.MAX_VALUE
        }
    }

}