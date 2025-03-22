package com.hearthappy.base.ext

import androidx.recyclerview.widget.GridLayoutManager
import com.hearthappy.base.AbsSpecialAdapter

/**
 * Created Date: 2025/3/22
 * @author ChenRui
 * ClassDescription：针对网格布局适配，头、尾、插入自定义布局占据一行空间
 */
fun GridLayoutManager.bindSpecialAdapter(specialAdapter: AbsSpecialAdapter<*, *>) {
    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when {
                specialAdapter.hasHeaderImpl() && position == 0 -> spanCount
                specialAdapter.hasFooterImpl() && position == itemCount - 1 -> spanCount
                specialAdapter.getCustomPositions().contains(position) -> spanCount
                else -> 1
            }
        }
    }
}