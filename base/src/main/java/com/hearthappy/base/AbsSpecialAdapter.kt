package com.hearthappy.base

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.interfaces.IEmptyViewSupport
import com.hearthappy.base.interfaces.IFooterSupport
import com.hearthappy.base.interfaces.IHeaderSupport
import com.hearthappy.base.interfaces.IInsetItemSupper
import com.hearthappy.base.interfaces.OnEmptyViewClickListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener
import com.hearthappy.base.interfaces.OnInsetItemClickListener
import java.lang.reflect.Method
import java.util.Collections


/**
 * Created Date: 2025/3/7
 * @author ChenRui
 * ClassDescription：特殊适配，支持头、尾、空布局AbsSpecialAdapter<ViewBinding类型,数据类型>()
 * 根据需求实现：IHeaderSupport、IFooterSupport、IEmptyViewSupport接口
 */
@Suppress("UNCHECKED_CAST")
abstract class AbsSpecialAdapter<VB : ViewBinding, T> : AbsBaseAdapter<VB, T>() {

    private var shouldShowEmptyView: Boolean = false
    private var onHeaderClickListener: OnHeaderClickListener? = null
    private var onFooterClickListener: OnFooterClickListener? = null
    private var onEmptyViewClickListener: OnEmptyViewClickListener? = null
    private var onInsetItemClickListener: OnInsetItemClickListener? = null
    private var headerOffset = 0
    private var footerOffset = 0

    private var insetItemPositions: List<Int> = listOf()
    private var insetItemLayouts: List<IInsetItemSupper<*>> = listOf()
    private var insetItemMap = mutableMapOf<Int, IInsetItemSupper<*>>()
    private var creatorCount = 0
    private var insetTransformMap = mutableMapOf<Int, Int>() //原索引，推算的索引

    /**
     * 设置插入布局
     * @param insetItemLayout List<IInsetItemSupper<*>> 插入布局的接口实现集合
     * @param insetItemPositions IntArray inset position
     * position range:P:-1：不插入 || P >=list.size：插入到item列表最后一条 || -1< P <list.size：插入到指定位置 ,传入空则不插入
     */
    fun setInsetItemLayout(insetItemLayout: List<IInsetItemSupper<*>>, vararg insetItemPositions: Int) {
        this.insetItemLayouts = insetItemLayout
        val insetList = insetItemPositions.toList()
        val transformList = transformList(insetList)
        if (this.insetItemPositions.isNotEmpty() && insetItemPositions.isEmpty() && insetItemLayouts.isEmpty()) { //清理插入布局
            this.insetItemPositions.forEach { if (it >= 0 && it < list.size) notifyItemRemoved(getVirtualPosition(it)) }
        } else {
            insetItemPositions.forEachIndexed { index, it ->
                val transformPosition = transformList[index]
                insetTransformMap[transformPosition] = it
                insetItemMap[transformPosition] = insetItemLayouts[index]
                notifyItemInserted(transformPosition)
            }
        }
        this.insetItemPositions = insetList
        notifyItemRangeChanged(headerOffset, getItemSpecialCount())
    }

    fun setOnHeaderClickListener(onHeaderClickListener: OnHeaderClickListener?) {
        this.onHeaderClickListener = onHeaderClickListener
    }

    fun setFooterClickListener(onFooterClickListener: OnFooterClickListener?) {
        this.onFooterClickListener = onFooterClickListener
    }

    fun setEmptyViewClickListener(onEmptyViewClickListener: OnEmptyViewClickListener?) {
        this.onEmptyViewClickListener = onEmptyViewClickListener
    }

    fun setInsetItemClickListener(onInsetItemClickListener: OnInsetItemClickListener?) {
        this.onInsetItemClickListener = onInsetItemClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> if (hasHeaderImpl()) return HeaderViewHolder(getIHeaderSupport().initHeaderBinding(parent, viewType))
            TYPE_EMPTY -> if (hasEmptyViewImpl()) return EmptyViewHolder(getIEmptyViewSupport().initEmptyBinding(parent, viewType))
            TYPE_FOOTER -> if (hasFooterImpl()) return FooterViewHolder(getIFooterSupport().initFooterBinding(parent, viewType))
            TYPE_INSET_ITEM -> {
                val insetItem = insetItemLayouts[creatorCount]
                creatorCount++
                if (creatorCount >= insetItemLayouts.size) {
                    creatorCount--
                }
                return InsetItemViewHolder(insetItem.initInsetItemBinding(parent, viewType))
            }
        }
        return ItemViewHolder(initViewBinding(parent, viewType))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbsSpecialAdapter<*, *>.EmptyViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onEmptyViewClickListener?.onEmptyViewClick(it) }
                callBindMethod(this, holder.viewBinding, "bindEmptyViewHolder")
            }

            is AbsSpecialAdapter<*, *>.HeaderViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it) }
                callBindMethod(this, holder.viewBinding, "bindHeaderViewHolder")
            }

            is AbsSpecialAdapter<*, *>.ItemViewHolder -> {
                val realPosition = getRealPosition(position)
                holder.viewBinding.root.setOnClickListener {
                    Log.d(TAG, "onBindViewHolder ItemViewHolder: $position,$realPosition")
                    onItemClickListener?.onItemClick(it, list[realPosition], realPosition)
                }
                (holder.viewBinding as VB).bindViewHolder(list[realPosition], realPosition)
            }

            is AbsSpecialAdapter<*, *>.InsetItemViewHolder -> {
                holder.viewBinding.root.setOnClickListener {
                    Log.d(TAG, "onBindViewHolder InsetItemViewHolder: $position")
                    onInsetItemClickListener?.onInsetItemClick(it)
                }
                insetItemMap[position]?.let { callBindMethod(it, holder.viewBinding, "bindInsetViewHolder") }
            }

            is AbsSpecialAdapter<*, *>.FooterViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onFooterClickListener?.onFooterClick(it) }
                callBindMethod(this, holder.viewBinding, "bindFooterViewHolder")
            }

            else -> Unit
        }
    }


    private fun getRealPosition(position: Int): Int {
        var realPosition = position - headerOffset
        for (insetPosition in insetItemPositions) {
            if (insetPosition > realPosition) {
                break
            }
            realPosition--
        }
        return realPosition
    }

    private fun getVirtualPosition(realPosition: Int): Int {
        var virtualPosition = realPosition + headerOffset
        for (insetPosition in insetItemPositions) {
            if (insetPosition > realPosition) {
                break
            }
            virtualPosition++
        }
        return virtualPosition
    }

    fun transformList(inputList: List<Int>): List<Int> {
        return inputList.mapIndexed { index, value -> // index 从 0 开始，所以要加 1 得到从 1 开始的位置
            value + (index + 1)
        }
    }

    override fun getItemViewType(position: Int): Int { // 计算累积的插入布局偏移量
        return when {
            hasEmptyViewImpl() && shouldShowEmptyView -> TYPE_EMPTY
            hasHeaderImpl() && position == TYPE_HEADER -> TYPE_HEADER
            hasFooterImpl() && position == headerOffset + list.size + insetItemLayouts.size -> TYPE_FOOTER //            hasInsetItemImpl() && insetItemPosition != NOT_INSERTED && insetItemPosition.convertInsetItemPosition() == position - headerOffset -> TYPE_INSET_ITEM
            insetItemLayouts.isNotEmpty() && insetItemPositions.any { it != NOT_INSERTED && it.convertInsetItemPosition() == insetTransformMap[position] } -> { //                ++insetItemOffset
                TYPE_INSET_ITEM
            }

            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        headerOffset = if (hasHeaderImpl()) 1 else 0
        footerOffset = if (hasFooterImpl()) 1 else 0
        return if (shouldShowEmptyView && hasEmptyViewImpl()) 1 else getItemSpecialCount()
    }


    override fun initData(list: List<T>) {
        if (this.list.isNotEmpty()) this.list.clear()
        notifyItemRangeRemoved(0,getItemSpecialCount())
        this.list.addAll(list)
        shouldShowEmptyView = list.isEmpty()
        notifyItemRangeChanged(0, if (shouldShowEmptyView) 1 else getItemSpecialCount())
    }

    override fun insertData(data: T) {
        val position = this.list.size
        val virtualPosition = getVirtualPosition(position)
        this.list.add(data)
        shouldShowEmptyView = false
        if (this.list.size == 1) { //首次插入一条时，将头布局和插入布局刷新出来
            notifyItemRangeChanged(0, getItemSpecialCount())
        } else {
            notifyItemInserted(virtualPosition)
            notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
        }
    }

    override fun insertData(data: T, position: Int) {
        this.list.add(position, data)
        val virtualPosition = getVirtualPosition(position)
        shouldShowEmptyView = false
        notifyItemInserted(virtualPosition)
        notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
    }

    override fun removeData(position: Int): T? {
        val virtualPosition = getVirtualPosition(position)
        if (position >= 0 && position < this.list.size) {
            val removedItem = this.list.removeAt(position)
            shouldShowEmptyView = this.list.isEmpty()
            notifyItemRemoved(virtualPosition)
            notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
            return removedItem
        }
        return null
    }


    override fun removeAll() {
        val size = this.list.size
        if (size > 0) {
            this.list.clear()
            if (hasHeaderImpl()) notifyItemRemoved(0)
            notifyItemRangeRemoved(headerOffset, size)
            if (hasFooterImpl()) notifyItemRemoved(footerOffset)
            shouldShowEmptyView = true
            notifyItemChanged(if (hasEmptyViewImpl()) 1 else 0)
        }
    }

    override fun addData(list: List<T>) {
        val oldPosition = this.list.size
        this.list.addAll(list)
        shouldShowEmptyView = this.list.isEmpty()
        notifyItemRangeChanged(oldPosition + headerOffset, this.list.size - oldPosition)
    }

    override fun addData(list: List<T>, position: Int) {
        this.list.addAll(position, list)
        shouldShowEmptyView = this.list.isEmpty()
        if (position == 0) {
            notifyItemRangeInserted(headerOffset, list.size)
        } else {
            notifyItemRangeChanged(position + headerOffset, this.list.size)
        }
    }

    override fun updateData(data: T, position: Int) {
        if (position >= this.list.size) return
        this.list[position] = data
        shouldShowEmptyView = false
        val virtualPosition = getVirtualPosition(position)
        notifyItemChanged(virtualPosition)
    }

    override fun moveData(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        Collections.swap(this.list, fromPosition, toPosition)
        val virtualFromPosition = getVirtualPosition(fromPosition)
        val virtualToPosition = getVirtualPosition(toPosition)
        notifyItemMoved(virtualFromPosition, virtualToPosition)
        notifyItemRangeChanged(virtualFromPosition, virtualToPosition)
    }

    private inner class HeaderViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class FooterViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class EmptyViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class InsetItemViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class ItemViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    private fun callBindMethod(support: Any, viewBinding: ViewBinding, methodName: String) {
        try {
            val method: Method = support.javaClass.getMethod(methodName, viewBinding.javaClass)
            method.invoke(support, viewBinding)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hasHeaderImpl() = this is IHeaderSupport<*>
    private fun hasFooterImpl() = this is IFooterSupport<*>
    private fun hasEmptyViewImpl() = this is IEmptyViewSupport<*>
    private fun hasInsetItemImpl() = this is IInsetItemSupper<*>

    private fun getIInsetItemSupper() = this as IInsetItemSupper<ViewBinding>
    private fun getIHeaderSupport() = this as IHeaderSupport<ViewBinding>
    private fun getIFooterSupport() = this as IFooterSupport<ViewBinding>
    private fun getIEmptyViewSupport() = this as IEmptyViewSupport<ViewBinding>
    private fun getItemSpecialCount() = list.size + headerOffset + insetItemLayouts.size + footerOffset

    private fun Int.convertInsetItemPosition(): Int {
        return if (this > list.size) list.size else this
    }

    companion object {
        private const val TAG = "AbsSpecialAdapter"
        const val TYPE_HEADER: Int = 0x00
        const val TYPE_ITEM: Int = 0x01
        const val TYPE_EMPTY: Int = 0x02
        const val TYPE_FOOTER: Int = 0x03
        const val TYPE_INSET_ITEM = 0x04
        const val NOT_INSERTED = 0xFFFFFFFF.toInt()
    }
}