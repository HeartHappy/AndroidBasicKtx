package com.hearthappy.base.ext

import androidx.recyclerview.widget.GridLayoutManager
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