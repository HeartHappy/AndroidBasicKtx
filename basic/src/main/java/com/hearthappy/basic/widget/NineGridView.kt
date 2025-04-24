package com.hearthappy.basic.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hearthappy.basic.ext.dp2px


/**
 * Created Date: 2025/4/24
 * @author ChenRui
 * ClassDescription：九宫格RecyclerView
 * 1、支持自定义不同列的item宽高，
 * 2、支持圆角
 * 3、支持自定义item
 */
class NineGridView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    // 300.dp2px(), 200.dp2px(), 120.dp2px()
    fun initData(data: List<String>, singleWidth: Int= 300.dp2px(), singleHeight: Int=200.dp2px(), proportional: Int=120.dp2px()) { // 根据数据数量设置列数
        val spanCount: Int = getSpanCount(data.size)
        val layoutManager = StaggeredGridLayoutManager(spanCount, GridLayoutManager.VERTICAL)
        setLayoutManager(layoutManager)
        adapter = NineGridAdapter(singleWidth, singleHeight, proportional).apply { initData(data.take(9)) }
    }

    private fun getSpanCount(itemCount: Int): Int {
        return when (itemCount) {
            1 -> 1
            2, 4 -> 2
            else -> 3
        }
    }
}