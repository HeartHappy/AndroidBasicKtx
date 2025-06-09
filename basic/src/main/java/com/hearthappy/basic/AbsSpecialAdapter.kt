package com.hearthappy.basic

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.hearthappy.basic.ext.findAdapterInflate
import com.hearthappy.basic.ext.findInterfaceInflate
import com.hearthappy.basic.interfaces.ICustomItemSupper
import com.hearthappy.basic.interfaces.IEmptyViewSupport
import com.hearthappy.basic.interfaces.IFooterSupport
import com.hearthappy.basic.interfaces.IHeaderSupport
import com.hearthappy.basic.interfaces.OnCustomItemClickListener
import com.hearthappy.basic.interfaces.OnEmptyViewClickListener
import com.hearthappy.basic.interfaces.OnFooterClickListener
import com.hearthappy.basic.interfaces.OnHeaderClickListener
import com.hearthappy.basic.model.CustomItemView
import java.lang.reflect.Method
import java.util.Collections


/**
 * Created Date: 2025/3/7
 * @author ChenRui
 * ClassDescription：特殊适配，支持头、尾、空、以及自定义布局。AbsSpecialAdapter<ViewBinding类型,数据类型>()
 * 根据需求实现：IHeaderSupport、IFooterSupport、IEmptyViewSupport、接口
 */

@Suppress("UNCHECKED_CAST")
abstract class AbsSpecialAdapter<VB : ViewBinding, T> : ISpecialAdapter<VB, T>() {

    private var shouldShowEmptyView: Boolean = false
    private var onHeaderClickListener: OnHeaderClickListener? = null
    private var onFooterClickListener: OnFooterClickListener? = null
    private var onEmptyViewClickListener: OnEmptyViewClickListener? = null
    private var onCustomItemClickListener: OnCustomItemClickListener<T>? = null
    private var headerOffset = 0
    private var footerOffset = 0

    private val customItems = mutableListOf<CustomItemView>()
    private val noCustomization = setOf(TYPE_HEADER, TYPE_FOOTER, TYPE_EMPTY, TYPE_ITEM) //  不包含自定义布局
    private val checkItemType = setOf(TYPE_HEADER, TYPE_FOOTER, TYPE_EMPTY)

    private var isHeaderFull = false
    private var isFooterFull = false
    private var isCustomFull = false
    private var isEmptyFull = false
    private var showEmptyAndHeader = true //同时显示空布局和头布局


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> if (hasHeaderImpl()) return HeaderViewHolder(getIHeaderSupport().javaClass.findInterfaceInflate(parent, IHeaderSupport::class.java))

            TYPE_EMPTY -> if (hasEmptyViewImpl()) return EmptyViewHolder(getIEmptyViewSupport().javaClass.findInterfaceInflate(parent, IEmptyViewSupport::class.java))

            TYPE_FOOTER -> if (hasFooterImpl()) return FooterViewHolder(getIFooterSupport().javaClass.findInterfaceInflate(parent, IFooterSupport::class.java))

            else -> {
                val customItem = customItems.firstOrNull { it.viewType == viewType }
                if (customItem != null) {
                    return CustomItemViewHolder(customItem.supper.javaClass.findInterfaceInflate(parent, ICustomItemSupper::class.java))
                }
            }

        }
        return ItemViewHolder(initViewBinding(parent, viewType) ?: findAdapterInflate(LayoutInflater.from(parent.context), parent))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbsSpecialAdapter<*, *>.EmptyViewHolder -> {
                holder.viewBinding.apply {
                    setItemFull(root, isEmptyFull)
                    root.setOnClickListener {
                        onEmptyViewClickListener?.onEmptyViewClick(it, position)
                    }
                    callBindMethod(this@AbsSpecialAdapter, this, BIND_EMPTY)
                }
            }

            is AbsSpecialAdapter<*, *>.HeaderViewHolder -> {
                holder.viewBinding.apply {
                    setItemFull(root, isHeaderFull)
                    root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it, position) }
                    callBindMethod(this@AbsSpecialAdapter, this, BIND_HEAD)
                }
            }

            is AbsSpecialAdapter<*, *>.ItemViewHolder -> {
                val listPosition = getItemListPosition(position)
                if (list.isEmpty()) return
                holder.viewBinding.apply {
                    root.setOnClickListener { onItemClickListener?.onItemClick(it, list[listPosition], position, listPosition) }
                    if (getItemSpecialCount() == itemCount) {
                        val realPosition = if (listPosition > list.size - 1) list.size - 1 else listPosition
                        (this as VB).bindViewHolder(list[realPosition], listPosition)
                    } else {
                        (this as VB).bindViewHolder(list[listPosition % itemRealCount], listPosition % itemRealCount)
                    }
                }
            }

            is AbsSpecialAdapter<*, *>.CustomItemViewHolder -> {
                val listPosition = getItemListPosition(position)
                val customItem = getCustomItemByPosition(position)
                if (customItem != null) {
                    holder.viewBinding.apply {
                        setItemFull(root, isCustomFull)
                        root.setOnClickListener { onCustomItemClickListener?.onCustomItemClick(it, list[listPosition], position, listPosition) }
                        callCustomBindMethod(customItem.supper, this, list[listPosition], listPosition)
                    }
                }
            }

            is AbsSpecialAdapter<*, *>.FooterViewHolder -> {
                holder.viewBinding.apply {
                    setItemFull(root, isFooterFull)
                    root.setOnClickListener { onFooterClickListener?.onFooterClick(it, position) }
                    callBindMethod(this@AbsSpecialAdapter, this, BIND_FOOTER)
                }
            }

            else -> Unit
        }
    }

    private fun setItemFull(view: View, isFull: Boolean) {
        if (isFull) {
            val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
            layoutParams ?: return
            layoutParams.isFullSpan = true
            view.layoutParams = layoutParams
        }
    }


    private fun getItemListPosition(position: Int): Int {
        return position - headerOffset
    }

    private fun getItemVirtualPosition(realPosition: Int): Int {
        return realPosition + headerOffset
    }


    fun addCustomItems(block: MutableList<CustomItemView>.() -> Unit) {
        block(customItems)
    }

    open fun getCustomItemViewType(data: T, position: Int): Int = -1

    private fun getCustomItemByPosition(position: Int): CustomItemView? {
        val viewType = getItemViewType(position)
        return customItems.firstOrNull { it.viewType == viewType }
    }


    override fun getItemViewType(position: Int): Int {
        val (customItemViewType: Int, isCustomLayout) = isCustomByPosition(position)
        return when {
            isHeaderPosition(position) -> TYPE_HEADER
            isEmptyView() -> TYPE_EMPTY
            isFooterPosition(position) -> TYPE_FOOTER
            isCustomLayout -> customItemViewType
            else -> TYPE_ITEM
        }
    }

    private fun isFooterPosition(position: Int) = hasFooterImpl() && position == getFooterPosition()
    private fun isEmptyViewWithHeader() = isEmptyView() && showEmptyAndHeader
    private fun isEmptyView() = hasEmptyViewImpl() && shouldShowEmptyView

    private fun isHeaderPosition(position: Int) = hasHeaderImpl() && position == getHeaderPosition() && (!shouldShowEmptyView || showEmptyAndHeader)

    private fun isCustomByPosition(position: Int): Pair<Int, Boolean> {
        var customItemViewType: Int = -1
        var isCustomLayout = false
        if (customItems.isNotEmpty() && list.isNotEmpty()) {
            customItemViewType = getCustomItemViewType(list[position.coerceIn(0, list.size - 1)], position)
            when {
                customItemViewType == -1 -> Log.e(TAG, "error: getCustomItemViewType method is not overridden")
                checkItemType.contains(customItemViewType) -> Log.e(TAG, "error: The type of custom layout, it is not recommended to use $TYPE_HEADER, $TYPE_FOOTER,$TYPE_EMPTY these will conflict with the header, footer, and empty layout types . position:$position")
                else -> isCustomLayout = true
            }
        }
        return Pair(customItemViewType, isCustomLayout)
    }

    override fun getItemCount(): Int {
        headerOffset = if (hasHeaderImpl()) 1 else 0
        footerOffset = if (hasFooterImpl()) 1 else 0
        return getItemSpecialCount()
    }

    /**
     * 调用数据初始化，
     * 1、如果先前数据与更新数据数量不一致或新数据数量为0，则会移除先移除先前数据
     * 2、如果相同，将通知数据内容发生改变，但是位置没有改变
     * @param list List<T>
     */
    fun initData(list: List<T>, useDataSetChanged: Boolean = false) {
        val size = this.list.size
        this.list = list.toMutableList()
        shouldShowEmptyView = list.isEmpty()
        if (useDataSetChanged) notifyGlobalRefresh()
        else {
            if (list.isEmpty() || size != list.size) notifyItemRangeRemoved(0, size)
            notifyItemRangeChanged(0, getItemSpecialCount())
            initRealItemCount()
        }
    }


    fun insertData(data: T, useDataSetChanged: Boolean = false) {
        val position = this.list.size
        val virtualPosition = getItemVirtualPosition(position)
        this.list.add(data)
        shouldShowEmptyView = false
        if (useDataSetChanged) notifyGlobalRefresh()
        else {
            notifyItemInserted(virtualPosition)
            notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
        }
    }

    fun insertData(data: T, position: Int, useDataSetChanged: Boolean = false) {
        this.list.add(position, data)
        val virtualPosition = getItemVirtualPosition(position)
        shouldShowEmptyView = false
        if (useDataSetChanged) notifyGlobalRefresh()
        else {
            notifyItemInserted(virtualPosition)
            notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
        }

    }

    fun removeData(position: Int, useDataSetChanged: Boolean = false): T? {
        val virtualPosition = getItemVirtualPosition(position)
        if (position >= 0 && position < this.list.size) {
            val removedItem = this.list.removeAt(position)
            shouldShowEmptyView = this.list.isEmpty()
            if (useDataSetChanged) notifyGlobalRefresh()
            else {
                notifyItemRemoved(virtualPosition)
                notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
            }

            return removedItem
        }
        return null
    }


    fun removeAll(useDataSetChanged: Boolean = false) {

        if (useDataSetChanged){
            list.clear()
            notifyGlobalRefresh()
        }
        else {
            val size = this.list.size
            if (size > 0) {
                if (hasHeaderImpl() && !showEmptyAndHeader) notifyItemRemoved(0)
                if (list.isNotEmpty()) list.clear()
                notifyItemRangeRemoved(headerOffset, size)
                if (hasFooterImpl()) notifyItemRemoved(footerOffset)
                shouldShowEmptyView = true
                notifyItemRangeChanged(0, getItemSpecialCount())
            }
        }


    }

    fun addData(list: List<T>, useDataSetChanged: Boolean = false) {
        val oldPosition = this.list.size
        this.list.addAll(list)
        shouldShowEmptyView = this.list.isEmpty()
        if (useDataSetChanged) {
            notifyGlobalRefresh()
        } else {
            notifyItemRangeChanged(oldPosition + headerOffset, getItemSpecialCount())
        }

    }

    fun addData(list: List<T>, position: Int, useDataSetChanged: Boolean = false) {
        val realPosition = getItemVirtualPosition(position)
        this.list.addAll(position, list)
        shouldShowEmptyView = this.list.isEmpty()
        if (useDataSetChanged) notifyGlobalRefresh()
        else notifyItemRangeChanged(realPosition, list.size)

    }

    fun updateData(data: T, position: Int, useDataSetChanged: Boolean = false) {
        if (position >= this.list.size) return
        this.list[position] = data
        shouldShowEmptyView = false
        val virtualPosition = getItemVirtualPosition(position)
        if (useDataSetChanged) notifyGlobalRefresh()
        else notifyItemChanged(virtualPosition)

    }

    fun moveData(fromPosition: Int, toPosition: Int, useDataSetChanged: Boolean = false) {
        if (fromPosition == toPosition) return
        if (fromPosition > getItemSpecialCount() || toPosition > getItemSpecialCount()) return
        Collections.swap(this.list, fromPosition, toPosition)
        if (useDataSetChanged) notifyGlobalRefresh()
        else {
            val virtualFromPosition = getItemVirtualPosition(fromPosition)
            val virtualToPosition = getItemVirtualPosition(toPosition)
            notifyItemMoved(virtualFromPosition, virtualToPosition)
            notifyItemRangeChanged(virtualFromPosition, virtualToPosition)
        }

    }

    @SuppressLint("NotifyDataSetChanged") fun notifyGlobalRefresh() {
        notifyDataSetChanged()
    }

    fun isCustomItemType(position: Int): Boolean {
        return !noCustomization.contains(getItemViewType(position))
    }

    //获取尾部局的Position，如果没有实现尾部局，则返回-1
    fun getFooterPosition(): Int {
        return if (hasFooterImpl()) headerOffset + list.size else -1
    }


    fun getHeaderPosition(): Int {
        return if (hasHeaderImpl()) 0 else -1
    }

    fun getEmptyPosition(): Int {
        return if (shouldShowEmptyView && hasEmptyViewImpl()) {
            if (showEmptyAndHeader) headerOffset else 0
        } else 0

    }

    fun getItemRealCount() = if (itemRealCount == itemCount) -1 else itemRealCount


    fun setOccupySpace(isHeaderFull: Boolean = true, isFooterFull: Boolean = true, isCustomFull: Boolean = true, isEmptyFull: Boolean) {
        this.isHeaderFull = isHeaderFull
        this.isFooterFull = isFooterFull
        this.isCustomFull = isCustomFull
        this.isEmptyFull = isEmptyFull
    }

    fun setShowEmptyAndHeader(isSimultaneously: Boolean) {
        showEmptyAndHeader = isSimultaneously
    }


    inner class HeaderViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    inner class FooterViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    inner class EmptyViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    inner class CustomItemViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    inner class ItemViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    private fun callBindMethod(support: Any, viewBinding: ViewBinding, methodName: String) {
        try {
            val method: Method = support.javaClass.getMethod(methodName, viewBinding.javaClass)
            method.invoke(support, viewBinding)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callCustomBindMethod(support: ICustomItemSupper<*, *>, viewBinding: ViewBinding, data: T, customPosition: Int, methodName: String = BIND_CUSTOM) {
        try {
            val method = support.javaClass.declaredMethods.first { it.name == methodName }
            method.invoke(support, viewBinding, data, customPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasHeaderImpl() = this is IHeaderSupport<*>
    fun hasFooterImpl() = this is IFooterSupport<*>
    fun hasEmptyViewImpl() = this is IEmptyViewSupport<*>
    fun hasCustomViewImpl() = this is ICustomItemSupper<*, *>
    private fun getIHeaderSupport() = this as IHeaderSupport<ViewBinding>
    private fun getIFooterSupport() = this as IFooterSupport<ViewBinding>
    private fun getIEmptyViewSupport() = this as IEmptyViewSupport<ViewBinding>
    private fun getItemSpecialCount(): Int {
        return when {
            isEmptyViewWithHeader() -> headerOffset + 1
            isEmptyView() -> 1
            else -> headerOffset + list.size + footerOffset
        }
    }


    private fun Int.convertInsetItemPosition(): Int = if (this > list.size) list.size else this


    fun setOnHeaderClickListener(onHeaderClickListener: OnHeaderClickListener?) {
        this.onHeaderClickListener = onHeaderClickListener
    }

    fun setOnFooterClickListener(onFooterClickListener: OnFooterClickListener?) {
        this.onFooterClickListener = onFooterClickListener
    }

    fun setOnEmptyViewClickListener(onEmptyViewClickListener: OnEmptyViewClickListener?) {
        this.onEmptyViewClickListener = onEmptyViewClickListener
    }

    fun setOnCustomItemClickListener(onCustomItemClickListener: OnCustomItemClickListener<T>?) {
        this.onCustomItemClickListener = onCustomItemClickListener
    }


    companion object {
        private const val TAG = "AbsSpecialAdapter"
        const val TYPE_HEADER: Int = 10
        const val TYPE_FOOTER: Int = 11
        const val TYPE_EMPTY: Int = 12
        const val TYPE_ITEM: Int = 13
        const val BIND_HEAD = "bindHeaderViewHolder"
        const val BIND_FOOTER = "bindFooterViewHolder"
        const val BIND_EMPTY = "bindEmptyViewHolder"
        const val BIND_CUSTOM = "bindCustomViewHolder"
    }

}
