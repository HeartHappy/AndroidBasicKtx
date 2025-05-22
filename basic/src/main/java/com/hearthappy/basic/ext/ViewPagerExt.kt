package com.hearthappy.basic.ext

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


fun ViewPager.addListener(onSelect: (Int) -> Unit, onPageScrolled: (Int, Float, Int) -> Unit = { p, po, pop -> }, onPageScrollStateChanged: (Int) -> Unit = {}) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            onSelect(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            onPageScrollStateChanged(state)
        }
    })
}

fun ViewPager.addStateAdapter(fragmentManager: FragmentManager, count: Int, item: (Int) -> Fragment) {
    adapter = object : FragmentStatePagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return count
        }

        override fun getItem(position: Int): Fragment {
            return item(position)
        }
    }
}

fun ViewPager.addAdapter(fragmentManager: FragmentManager, count: Int, item: (Int) -> Fragment) {
    adapter = object : FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return count
        }

        override fun getItem(position: Int): Fragment {
            return item(position)
        }
    }
}


fun ViewPager2.addStateAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, count: Int, item: (Int) -> Fragment) {
    adapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = count
        override fun createFragment(position: Int): Fragment = item(position)
    }
}


fun ViewPager2.addListener(onSelect: (Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onSelect(position)
        }
    })
}


/**
 * 基于ViewPager2实现轮播图动画
 * @receiver ViewPager2
 * @param item Int
 * @param duration Long
 * @param interpolator TimeInterpolator
 */
fun ViewPager2.scrollAnimator(item: Int, duration: Long, interpolator: TimeInterpolator = AccelerateDecelerateInterpolator()) {
    val offsetDistance: Int = if (orientation == ViewPager2.ORIENTATION_VERTICAL) height else width
    val pxToDrag: Int = offsetDistance * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) { //            Log.d(CarouselViewPager2.TAG, "onAnimationStart: $item")
            beginFakeDrag()
        }

        override fun onAnimationEnd(animation: Animator) { //            Log.d(CarouselViewPager2.TAG, "onAnimationEnd: $item")
            //            val aiRecordBean = adapter.bannerList[item % Constant.QUERY_FIRST_NUMBER]
            //            val blurBitmap = aiRecordBean.imgBase64.base64ToBitmap().blur(this@scrollAnimator.context, 2f)
            //            Glide.with(this@scrollAnimator.context).load(blurBitmap).dontAnimate().into(imgView)
            //            bannerBlurBlock(blurBitmap)
            endFakeDrag()
        }

        override fun onAnimationCancel(animation: Animator) { /* Ignored */
        }

        override fun onAnimationRepeat(animation: Animator) { /* Ignored */
        }
    })
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
}

fun ViewPager2.getRecyclerView(): RecyclerView {
    return this.getChildAt(0) as RecyclerView
}

fun ViewPager2.findCurrentItemViewHolder(): RecyclerView.ViewHolder? {
    return getRecyclerView().findViewHolderForAdapterPosition(currentItem)
}