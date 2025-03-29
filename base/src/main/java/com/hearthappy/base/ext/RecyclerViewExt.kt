package com.hearthappy.base.ext

import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.AbsSpecialAdapter

fun RecyclerView.addFastListener(duration: Long = 500L, block: (Boolean) -> Unit) {
    var isAtTop = false
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            if (layoutManager != null) {
                val fastVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition() // 检查是否滚动到顶部
                isAtTop = fastVisibleItemPosition == 0
            }
        }
    })
    addOnTouchListener(duration, { isAtTop }) { block(it) }
}

fun RecyclerView.addLastListener(duration: Long = 500L, block: (Boolean) -> Unit) {
    var isAtBottom = false

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            if (layoutManager != null) {
                val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val itemCount = layoutManager.itemCount // 检查是否滚动到底部
                isAtBottom = lastVisibleItemPosition >= itemCount - 1 /*&& dy > 0*/
            }
        }
    })
    addOnTouchListener(duration, { isAtBottom }) { block(it) }

}

fun RecyclerView.addOnTouchListener(duration: Long, isTopOrBottomBlock: () -> Boolean, block: (Boolean) -> Unit) {
    var isDragging = false
    var touchDownTime = 0L
    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownTime = System.currentTimeMillis()
                    isDragging = true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isTopOrBottomBlock() && isDragging) {
                        val elapsedTime = System.currentTimeMillis() - touchDownTime
                        if (elapsedTime >= duration) { // 下拉超过 2 秒，触发刷新操作
                            post { block(true) }
                        }
                    }
                    isDragging = false
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    })
}

@Suppress("UNCHECKED_CAST")
fun <T : ViewBinding> RecyclerView.findFooterViewBinding(block: T.() -> Unit) {
    when (adapter) {
        is AbsSpecialAdapter<*, *> -> {
            val absSpecialAdapter = adapter as AbsSpecialAdapter<*, *>
            val footerPosition = absSpecialAdapter.getFooterPosition()
            if (footerPosition != -1) {
                val footerViewHolder = findViewHolderForAdapterPosition(footerPosition) as AbsSpecialAdapter<*, *>.FooterViewHolder
                block(footerViewHolder.viewBinding as T)
            }
        }

        else -> Unit
    }

}

fun RecyclerView.getFirstCompletelyVisiblePosition(): Int {
    val layoutManager = layoutManager as? LinearLayoutManager
    return layoutManager?.findFirstCompletelyVisibleItemPosition() ?: RecyclerView.NO_POSITION
}

fun RecyclerView.smoothScroller(targetPosition: Int, duration: Int = 200) { // 获取 RecyclerView 的 LayoutManager
    val layoutManager = layoutManager as? LinearLayoutManager
    val smoothScroller = object : LinearSmoothScroller(context) {
        override fun calculateTimeForScrolling(dx: Int): Int {
            return duration
        }
    }
    smoothScroller.targetPosition = targetPosition
    layoutManager?.startSmoothScroll(smoothScroller)
}
