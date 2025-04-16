package com.hearthappy.base.ext

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hearthappy.base.AbsSpecialAdapter

/**
 * Created Date: 2025/3/22
 * @author ChenRui
 * ClassDescription：针对网格布局适配，头、尾、插入自定义布局占据一行空间
 */
fun GridLayoutManager.bindSpecialAdapter(specialAdapter: AbsSpecialAdapter<*, *>, isRefreshFull: Boolean = true, isHeaderFull: Boolean = true, isFooterFull: Boolean = true, isCustomFull: Boolean = true) {
    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when {
                specialAdapter.hasRefreshImpl() && position == 0 && isRefreshFull -> spanCount
                specialAdapter.hasHeaderImpl() && specialAdapter.hasRefreshImpl() && position == 1 && isHeaderFull -> spanCount
                specialAdapter.hasFooterImpl() && position == itemCount - 1 && isFooterFull -> spanCount
                specialAdapter.getCustomPositions().contains(position) && isCustomFull -> spanCount
                else -> 1
            }
        }
    }
}
fun StaggeredGridLayoutManager.bindSpecialAdapter(rv: RecyclerView, specialAdapter: AbsSpecialAdapter<*, *>, isRefreshFull: Boolean = true, isHeaderFull: Boolean = true, isFooterFull: Boolean = true, isCustomFull: Boolean = true){
    // 监听 RecyclerView 的视图绑定事件，在绑定 ViewHolder 时设置布局参数
    specialAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            rv.post {
                rv.findViewHolderForAdapterPosition(0)?.itemView?.let { view ->
                    val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                    if (specialAdapter.hasRefreshImpl() && isRefreshFull) {
                        layoutParams?.isFullSpan = true
                    } else {
                        layoutParams?.isFullSpan = false
                    }
                    view.layoutParams = layoutParams
                }
                // 处理 header
                if (specialAdapter.hasHeaderImpl() && specialAdapter.hasRefreshImpl()) {
                    rv.findViewHolderForAdapterPosition(1)?.itemView?.let { view ->
                        val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                        if (isHeaderFull) {
                            layoutParams?.isFullSpan = true
                        } else {
                            layoutParams?.isFullSpan = false
                        }
                        view.layoutParams = layoutParams
                    }
                }
                // 处理 footer
                rv.findViewHolderForAdapterPosition(itemCount - 1)?.itemView?.let { view ->
                    val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                    if (specialAdapter.hasFooterImpl() && isFooterFull) {
                        layoutParams?.isFullSpan = true
                    } else {
                        layoutParams?.isFullSpan = false
                    }
                    view.layoutParams = layoutParams
                }
                // 处理自定义位置
                specialAdapter.getCustomPositions().forEach { position ->
                    rv.findViewHolderForAdapterPosition(position)?.itemView?.let { view ->
                        val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                        if (isCustomFull) {
                            layoutParams?.isFullSpan = true
                        } else {
                            layoutParams?.isFullSpan = false
                        }
                        view.layoutParams = layoutParams
                    }
                }
            }
        }
    })
}