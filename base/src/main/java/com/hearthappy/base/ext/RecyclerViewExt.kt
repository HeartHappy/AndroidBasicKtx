package com.hearthappy.base.ext

import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.AbsSpecialAdapter


/**
 * 监听第一个完全可见Item
 * @receiver RecyclerView
 * @param block Function1<Boolean, Unit>
 */
fun RecyclerView.addFastListener(block: (Boolean) -> Unit) {
    var isInitialLoad = true
    var isFirstItemVisible: Boolean
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            if (layoutManager != null) {
                val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition() // 检查是否滚动到顶部，第一个 item 完全可见
                isFirstItemVisible = firstVisibleItemPosition == 0
                if (!isInitialLoad && isFirstItemVisible) post { block(true) }
                else block(false) // 初始加载完成后，将标志位设为 false
                if (isInitialLoad) isInitialLoad = false
            }
        }
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

private fun <VB : ViewBinding> RecyclerView.addOnTouchListener(duration: Long, isTopOrBottomBlock: () -> Boolean, block: VB.() -> Unit) {
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
                        if (elapsedTime >= duration) { // 超过 duration，触发刷新或者加载操作
                            post { findHeaderViewBinding<VB> { block() } }
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
fun <T : ViewBinding> RecyclerView.findFooterViewBinding(delayed: Long = 1000L, block: T.() -> Unit) {
    when (adapter) {
        is AbsSpecialAdapter<*, *> -> {
            val absSpecialAdapter = adapter as AbsSpecialAdapter<*, *>
            val footerPosition = absSpecialAdapter.getFooterPosition()
            if (footerPosition != RecyclerView.NO_POSITION) {
                val footerViewHolder = findViewHolderForAdapterPosition(footerPosition) as AbsSpecialAdapter<*, *>.FooterViewHolder
                val vb = footerViewHolder.viewBinding as T
                block(vb)
                vb.root.postDelayed({
                    val visiblePosition = getFirstCompletelyVisiblePosition()
                    val lastVisiblePosition = getLastCompletelyVisiblePosition()
                    if (lastVisiblePosition == absSpecialAdapter.getFooterPosition()) smoothScroller(visiblePosition - 1)
                }, delayed)
            }
        }

        else -> Unit
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : ViewBinding> RecyclerView.findHeaderViewBinding(delayed: Long = 1000L, block: T.() -> Unit) {
    when (adapter) {
        is AbsSpecialAdapter<*, *> -> {
            val absSpecialAdapter = adapter as AbsSpecialAdapter<*, *>
            val headerPosition = absSpecialAdapter.getHeaderPosition()
            if (headerPosition != RecyclerView.NO_POSITION) {
                val headerViewHolder = findViewHolderForAdapterPosition(headerPosition) as? AbsSpecialAdapter<*, *>.HeaderViewHolder
                headerViewHolder ?: return
                val vb = headerViewHolder.viewBinding as T
                block(vb)
                vb.root.postDelayed({
                    val firstVisible = getFirstCompletelyVisiblePosition()
                    val visiblePosition = getLastCompletelyVisiblePosition()
                    if (firstVisible == 0) smoothScroller(visiblePosition + 1)
                }, delayed)
            }
        }
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
    }.also { it.targetPosition = targetPosition })
}

fun <VB : ViewBinding> RecyclerView.addLoadMoreListener(block: VB.() -> Unit) {
    addLastListener {
        findFooterViewBinding<VB> { block() }
    }
}

fun <VB : ViewBinding> RecyclerView.addOnRefreshListener(duration: Long = 50L, block: VB.() -> Unit) {
    var isAtTop = false
    addFastListener { isAtTop = true }
    addOnTouchListener<VB>(duration, { isAtTop }) { block() }
    postDelayed({
        val visiblePosition = getLastCompletelyVisiblePosition()
        smoothScrollToPosition(visiblePosition + 1)
    }, 50)
}
