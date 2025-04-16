package com.hearthappy.base.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.AbsSpecialAdapter
import com.hearthappy.base.ext.addFastListener
import com.hearthappy.base.ext.addLastListener
import com.hearthappy.base.ext.findFooterViewBinding
import com.hearthappy.base.ext.findRefreshViewBinding
import com.hearthappy.base.ext.getLastVisiblePosition
import com.hearthappy.base.ext.invisible
import com.hearthappy.base.ext.visible
import com.hearthappy.base.tools.TimeTools
import kotlin.math.abs

class RecyclerViewX : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)


    private var adapterDataObserver: AdapterDataObserver? = null


    private fun hideRefreshLayout() {
        TimeTools.continuousClick(100) {
            postDelayed({
                findRefreshViewBinding<ViewBinding> {
                    root.invisible()
                    smoothScrollBy(0, getRefreshVisibleHeight())
                }
            }, 50)
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) { // 移除旧的观察者
        adapterDataObserver?.let { getAdapter()?.unregisterAdapterDataObserver(it) }
        adapter ?: return
        adapterDataObserver = object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                Log.d("TAG", "onChanged: ")
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload) // 部分数据更新时调用，携带有效负载
                onAdapterDataChanged(positionStart, itemCount)
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver!!)
        super.setAdapter(adapter)
    }

    private fun onAdapterDataChanged(positionStart: Int, itemCount: Int) {
        if (positionStart != NO_POSITION && itemCount != NO_POSITION && positionStart == 0 && itemCount > 1) {
            hideRefreshLayout()
        }
    }

    private fun RecyclerView.addOnTouchListener(offsetThreshold: Int, percentageListener: (Float) -> Unit, actionUp: (Float) -> Unit) {
        var isDragging = false
        var initialY = 0f
        var currentOffset = 0
        var clampedPercentage = 0f

        addOnItemTouchListener(object : OnItemTouchListener {
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
                            percentageListener(clampedPercentage)
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isDragging = false
                        actionUp(clampedPercentage)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })
    }


    fun setOccupySpace(isRefreshFull: Boolean = true, isHeaderFull: Boolean = true, isFooterFull: Boolean = true, isCustomFull: Boolean = true) {
        val specialAdapter = adapter as? AbsSpecialAdapter<*, *>
        specialAdapter ?: return
        when (layoutManager) {
            is GridLayoutManager -> {
                val gridLayoutManager = layoutManager as GridLayoutManager
                val spanCount = gridLayoutManager.spanCount
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when {
                            specialAdapter.hasRefreshImpl() && position == specialAdapter.getRefreshPosition() && isRefreshFull -> spanCount
                            specialAdapter.hasHeaderImpl() && position == specialAdapter.getHeaderPosition() && isHeaderFull -> spanCount
                            specialAdapter.hasFooterImpl() && position == specialAdapter.getFooterPosition() && isFooterFull -> spanCount
                            specialAdapter.getCustomPositions().contains(position) && isCustomFull -> spanCount
                            else -> 1
                        }
                    }
                }
            }
            is StaggeredGridLayoutManager -> {
                specialAdapter.setOccupySpace(isRefreshFull, isHeaderFull, isFooterFull, isCustomFull)
            }
            else -> {}
        }
    }

    fun <VB : ViewBinding> addOnLoadMoreListener(delayed: Long = 50L, listener: VB.() -> Unit) {
        addLastListener {
            findFooterViewBinding<VB> { abs ->
                listener(this)
                this.root.postDelayed({
                    if (getLastVisiblePosition() == abs.getFooterPosition()) smoothScrollBy(0, getFooterVisibleHeight())
                }, delayed)
            }
        }
    }

    fun <VB : ViewBinding> addOnRefreshListener(offsetThreshold: Int = 800, onRefreshProgress: VB.(Float) -> Unit, onRefreshFinish: VB.() -> Unit) {
        addFastListener { hideRefreshLayout() }
        addOnTouchListener(offsetThreshold, { percentage ->
            findRefreshViewBinding<VB> {
                root.visible()
                onRefreshProgress(this, percentage)
            }
        }) { percentage ->
            findRefreshViewBinding<VB> {
                if (percentage >= 100f) {
                    root.invisible()
                    onRefreshFinish()
                }
            }
        }
    }


    private fun ViewBinding.getFooterVisibleHeight(): Int {
        val recyclerViewBottom = height - paddingBottom
        return root.bottom - root.height - recyclerViewBottom
    }

    private fun ViewBinding.getRefreshVisibleHeight(): Int {
        return root.height + root.top - paddingTop
    }
}