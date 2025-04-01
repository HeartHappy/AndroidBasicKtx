package com.hearthappy.base.ext

import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.AbsSpecialAdapter
import kotlin.math.abs


/**
 * 监听第一个完全可见Item
 * @receiver RecyclerView
 * @param block Function1<Boolean, Unit>
 */
fun RecyclerView.addFastListener(block: () -> Unit) {
    var isInitialLoad = true
    var isFirstItemVisible: Boolean
    addOnScrollListener(object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager != null) {
                    val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition() // 检查是否滚动到顶部，第一个 item 完全可见
                    isFirstItemVisible = firstVisibleItemPosition == 0
                    if (!isInitialLoad && isFirstItemVisible) block()
                    if (isInitialLoad) isInitialLoad = false
                }
            }
        } //        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { //            super.onScrolled(recyclerView, dx, dy) //            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        //            if (layoutManager != null) {
        //                val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition() // 检查是否滚动到顶部，第一个 item 完全可见
        //                isFirstItemVisible = firstVisibleItemPosition == 0
        //                if (!isInitialLoad && isFirstItemVisible) post { block(true) }
        //                else block(false) // 初始加载完成后，将标志位设为 false
        //                if (isInitialLoad) isInitialLoad = false
        //            }
        //        }
    })
}

/**
 * 监听最后一个完全可见Item
 * @receiver RecyclerView
 * @param block Function1<Boolean, Unit>
 */
fun RecyclerView.addLastListener(block: () -> Unit) {
    var isAtBottom: Boolean
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            if (layoutManager != null) {
                val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val itemCount = layoutManager.itemCount // 检查是否滚动到底部
                isAtBottom = lastVisibleItemPosition >= itemCount - 1
                if (isAtBottom) post { block() }
            }
        }
    })
}

private fun RecyclerView.addOnTouchListener(offsetThreshold: Int, percentageListener: (Float) -> Unit, actionUp: (Float) -> Unit) {
    var isDragging = false
    var initialY = 0f
    var currentOffset = 0
    var clampedPercentage = 0f

    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = e.y
                    isDragging = true
                    currentOffset = 0
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val deltaY = (e.y - initialY).toInt()
                        currentOffset += deltaY
                        initialY = e.y

                        // 计算当前偏移量相对于阈值的百分比
                        val percentage = abs(currentOffset).toFloat() / offsetThreshold.toFloat()
                        clampedPercentage = percentage.coerceIn(0f, 1f) * 100
                        percentageListener(clampedPercentage) // 无论是否达到阈值，都传递百分比给 block 函数
                        //                        if (isAtTop && abs(currentOffset) >= offsetThreshold) {
                        //                            isDragging = false
                        //                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    actionUp(clampedPercentage)
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    })
}


@Suppress("UNCHECKED_CAST")
fun <T : ViewBinding> RecyclerView.findRefreshViewBinding(block: T.(AbsSpecialAdapter<*, *>) -> Unit) {
    when (adapter) {
        is AbsSpecialAdapter<*, *> -> {
            val absSpecialAdapter = adapter as AbsSpecialAdapter<*, *>
            val refreshPosition = absSpecialAdapter.getRefreshPosition()
            if (refreshPosition != RecyclerView.NO_POSITION) {
                val refreshViewHolder = findViewHolderForAdapterPosition(refreshPosition) as? AbsSpecialAdapter<*, *>.RefreshViewHolder
                refreshViewHolder ?: return
                val vb = refreshViewHolder.viewBinding as T
                block(vb, absSpecialAdapter)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : ViewBinding> RecyclerView.findHeaderViewBinding(block: T.(AbsSpecialAdapter<*, *>) -> Unit) {
    when (adapter) {
        is AbsSpecialAdapter<*, *> -> {
            val absSpecialAdapter = adapter as AbsSpecialAdapter<*, *>
            val headerPosition = absSpecialAdapter.getHeaderPosition()
            if (headerPosition != RecyclerView.NO_POSITION) {
                val headerViewHolder = findViewHolderForAdapterPosition(headerPosition) as? AbsSpecialAdapter<*, *>.HeaderViewHolder
                headerViewHolder ?: return
                val vb = headerViewHolder.viewBinding as T
                block(vb, absSpecialAdapter)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : ViewBinding> RecyclerView.findFooterViewBinding(block: T.(AbsSpecialAdapter<*, *>) -> Unit) {
    when (adapter) {
        is AbsSpecialAdapter<*, *> -> {
            val absSpecialAdapter = adapter as AbsSpecialAdapter<*, *>
            val footerPosition = absSpecialAdapter.getFooterPosition()
            if (footerPosition != RecyclerView.NO_POSITION) {
                val footerViewHolder = findViewHolderForAdapterPosition(footerPosition) as AbsSpecialAdapter<*, *>.FooterViewHolder
                val vb = footerViewHolder.viewBinding as T
                block(vb, absSpecialAdapter)
            }
        }

        else -> Unit
    }
}

fun RecyclerView.getFirstCompletelyVisiblePosition(): Int {
    val layoutManager = layoutManager as? LinearLayoutManager
    return layoutManager?.findFirstCompletelyVisibleItemPosition() ?: RecyclerView.NO_POSITION
}

fun RecyclerView.getLastCompletelyVisiblePosition(): Int {
    val layoutManager = layoutManager as? LinearLayoutManager
    return layoutManager?.findLastCompletelyVisibleItemPosition() ?: RecyclerView.NO_POSITION
}

fun RecyclerView.smoothScroller(targetPosition: Int, duration: Int = 100) { // 获取 RecyclerView 的 LayoutManager
    if (targetPosition == RecyclerView.NO_POSITION) return
    val layoutManager = layoutManager as? LinearLayoutManager
    layoutManager?.startSmoothScroll(object : LinearSmoothScroller(context) {
        override fun calculateTimeForScrolling(dx: Int): Int = duration
    }.also {
        it.targetPosition = targetPosition
    })
}

fun <VB : ViewBinding> RecyclerView.addOnLoadMoreListener(delayed: Long = 1000L, listener: VB.() -> Unit) {
    addLastListener {
        findFooterViewBinding<VB> { abs ->
            listener()
            this.root.postDelayed({
                val lastVisiblePosition = getLastCompletelyVisiblePosition()
                val footerHeight = this.root.height
                if (lastVisiblePosition == abs.getFooterPosition()) smoothScrollBy(0, -footerHeight)
            }, delayed)
        }
    }
}


fun <VB : ViewBinding> RecyclerView.addOnRefreshListener(delayed: Long = 200L, offsetThreshold: Int = 800, onRefreshProgress: VB.(Float) -> Unit, onRefreshFinish: VB.() -> Unit) {
    addFastListener { postDelayed({ findRefreshViewBinding<VB> { smoothScrollBy(0, root.height) } }, 50) }
    addOnTouchListener(offsetThreshold, { percentage ->
        findRefreshViewBinding<VB> { onRefreshProgress(this, percentage) }
    }) { percentage ->
        findRefreshViewBinding<VB> {
            if (percentage >= 100f) onRefreshFinish()
            postDelayed({ if (getFirstCompletelyVisiblePosition() == 0) smoothScrollBy(0, root.height) }, delayed)
        }
    }
    postDelayed({ findRefreshViewBinding<VB> { smoothScrollBy(0, root.height) } }, 0)
}