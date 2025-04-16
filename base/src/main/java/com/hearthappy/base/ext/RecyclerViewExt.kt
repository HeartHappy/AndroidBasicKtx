package com.hearthappy.base.ext

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.AbsSpecialAdapter
import java.io.Serializable


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
                val lm = recyclerView.layoutManager
                if (lm != null) {
                    when (lm) {
                        is LinearLayoutManager -> {
                            val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()
                            isFirstItemVisible = firstVisibleItemPosition == 0
                        }
                        is GridLayoutManager -> {
                            val firstVisibleItemPosition = lm.findFirstVisibleItemPosition() // 对于网格布局，需要根据列数来判断是否滚动到底部
                            isFirstItemVisible = firstVisibleItemPosition == 0
                        }
                        is StaggeredGridLayoutManager -> {
                            val firstVisibleItemPositions: IntArray = lm.findFirstVisibleItemPositions(null)
                            var firstVisibleItemPosition = Int.MAX_VALUE
                            for (position in firstVisibleItemPositions) {
                                if (position < firstVisibleItemPosition) {
                                    firstVisibleItemPosition = position
                                }
                            }
                            isFirstItemVisible = firstVisibleItemPosition == 0
                        }
                        else -> {
                            isFirstItemVisible = false
                        }
                    }
                    if (isFirstItemVisible) block() //                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition() // 检查是否滚动到顶部，第一个 item 完全可见
                    //                    isFirstItemVisible = firstVisibleItemPosition == 0

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
    addOnScrollListener(object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {
                val lm = recyclerView.layoutManager
                val itemCount = layoutManager?.itemCount ?: 0
                lm?.let {
                    when (it) {
                        is LinearLayoutManager -> {
                            val lastVisibleItemPosition = it.findLastVisibleItemPosition()
                            isAtBottom = lastVisibleItemPosition >= itemCount - 1
                        }
                        is StaggeredGridLayoutManager -> {
                            val lastVisibleItemPositions = it.findLastVisibleItemPositions(null)
                            val maxLastVisibleItemPosition = lastVisibleItemPositions.maxOrNull() ?: 0
                            isAtBottom = maxLastVisibleItemPosition >= itemCount - 1
                        }
                        is GridLayoutManager -> {
                            val lastVisibleItemPosition = it.findLastVisibleItemPosition() // 对于网格布局，需要根据列数来判断是否滚动到底部
                            val spanCount = it.spanCount
                            val lastRowStartIndex = (itemCount - 1) / spanCount * spanCount
                            isAtBottom = lastVisibleItemPosition >= lastRowStartIndex
                        }
                        else -> {
                            isAtBottom = false
                        }
                    }
                    if (isAtBottom) block()
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

fun RecyclerView.getLastVisiblePosition(): Serializable {
    return when (layoutManager) {
        is LinearLayoutManager -> {
            (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
        }
        is StaggeredGridLayoutManager -> {
            (layoutManager as? StaggeredGridLayoutManager)?.findLastVisibleItemPositions(null)?.maxOrNull() ?: RecyclerView.NO_POSITION
        }
        else -> {
            (layoutManager as? GridLayoutManager)?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
        }
    }
}

fun RecyclerView.smoothScroller(targetPosition: Int, duration: Int = 100) { // 获取 RecyclerView 的 LayoutManager
    if (targetPosition == RecyclerView.NO_POSITION) return
    layoutManager?.startSmoothScroll(object : LinearSmoothScroller(context) {
        override fun calculateTimeForScrolling(dx: Int): Int = duration
    }.also {
        it.targetPosition = targetPosition
    })
}







