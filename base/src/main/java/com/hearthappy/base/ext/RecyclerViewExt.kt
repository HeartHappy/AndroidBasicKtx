package com.hearthappy.base.ext

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.AbsSpecialAdapter


/**
 * 监听第一个完全可见Item
 * @receiver RecyclerView
 * @param block Function1<Boolean, Unit>
 */
fun RecyclerView.addFastListener(block: () -> Unit) {
    var isFirstItemVisible: Boolean
    addOnScrollListener(object : OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager != null) {
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition() // 检查是否滚动到顶部，第一个 item 完全可见
                    isFirstItemVisible = firstVisibleItemPosition == 0
                    if (isFirstItemVisible) block()
                }
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
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager != null) {
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val itemCount = layoutManager.itemCount // 检查是否滚动到底部
                    isAtBottom = lastVisibleItemPosition >= itemCount - 1
                    if (isAtBottom) post { block() }
                }
            }
        }
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
                val footerViewHolder = findViewHolderForAdapterPosition(footerPosition) as? AbsSpecialAdapter<*, *>.FooterViewHolder
                footerViewHolder ?: return
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

fun RecyclerView.getFirstVisiblePosition(): Int {
    val layoutManager = layoutManager as? LinearLayoutManager
    return layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
}

fun RecyclerView.getLastVisiblePosition(): Int {
    val layoutManager = layoutManager as? LinearLayoutManager
    return layoutManager?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
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







