package com.hearthappy.basic

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
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
import com.hearthappy.basic.interfaces.OnItemClickListener
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
abstract class AbsSpecialAdapter<VB : ViewBinding, T>(
    var list: MutableList<T> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var itemRealCount = -1 //真实数量，例如：轮播真实5条，展示列表7条（首尾补位）时用于记录真实数量
    internal var onItemClickListener: OnItemClickListener<T>? = null
    private var infiniteLoopEnabled = false
    private var isRealItemCountOverridden = false

    private var shouldShowEmptyView = false
    private var onHeaderClickListener: OnHeaderClickListener? = null
    private var onFooterClickListener: OnFooterClickListener? = null
    private var onEmptyViewClickListener: OnEmptyViewClickListener? = null
    private var onCustomItemClickListener: OnCustomItemClickListener<T>? = null

    private var headerView: IHeaderSupport<*>? = null
    private var footerView: IFooterSupport<*>? = null
    private val customItems = mutableListOf<CustomItemView>()
    private val noCustomization = setOf(TYPE_HEADER, TYPE_FOOTER, TYPE_EMPTY, TYPE_ITEM)
    private val checkItemType = setOf(TYPE_HEADER, TYPE_FOOTER, TYPE_EMPTY)

    private var isHeaderFull = false
    private var isFooterFull = false
    private var isCustomFull = false
    private var isEmptyFull = false
    private var showEmptyAndHeader = true //同时显示空布局和头布局

    open fun initViewBinding(parent: ViewGroup, viewType: Int): VB? = null

    abstract fun VB.bindViewHolder(data: T, position: Int)

    fun initRealItemCount() {
        isRealItemCountOverridden = false
        itemRealCount = list.size
    }

    fun setRealItemCount(realCount: Int) {
        isRealItemCountOverridden = true
        itemRealCount = realCount.coerceAtLeast(0)
    }

    fun clearRealItemCountOverride() {
        initRealItemCount()
    }

    fun setInfiniteLoopEnabled(enabled: Boolean, notifyImmediately: Boolean = true) {
        if (infiniteLoopEnabled == enabled) return
        infiniteLoopEnabled = enabled
        if (notifyImmediately) {
            syncDisplayState()
            notifyGlobalRefresh()
        }
    }

    fun getInfiniteLoopStartPosition(): Int {
        if (!isInfiniteLoopMode()) return 0
        val anchor = Int.MAX_VALUE / 2
        return anchor - Math.floorMod(anchor, list.size)
    }

    fun getRealPosition(adapterPosition: Int): Int {
        if (list.isEmpty()) return -1
        return if (isInfiniteLoopMode()) {
            Math.floorMod(adapterPosition, list.size)
        } else {
            adapterPosition - getDisplayedHeaderCount()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> if (hasHeaderImpl()) {
                return HeaderViewHolder(getIHeaderSupport().javaClass.findInterfaceInflate(parent, IHeaderSupport::class.java))
            }

            TYPE_EMPTY -> if (hasEmptyViewImpl()) {
                return EmptyViewHolder(getIEmptyViewSupport().javaClass.findInterfaceInflate(parent, IEmptyViewSupport::class.java))
            }

            TYPE_FOOTER -> if (hasFooterImpl()) {
                return FooterViewHolder(getIFooterSupport().javaClass.findInterfaceInflate(parent, IFooterSupport::class.java))
            }

            else -> {
                val customItem = customItems.firstOrNull { it.viewType == viewType }
                if (customItem != null) {
                    return CustomItemViewHolder(customItem.supper.javaClass.findInterfaceInflate(parent, ICustomItemSupper::class.java))
                }
            }
        }
        return ItemViewHolder(initViewBinding(parent, viewType) ?: findAdapterInflate(LayoutInflater.from(parent.context), parent))
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_EMPTY -> bindEmptyViewHolder(holder as EmptyViewHolder, position)
            TYPE_HEADER -> bindHeaderViewHolder(holder as HeaderViewHolder, position)
            TYPE_FOOTER -> bindFooterViewHolder(holder as FooterViewHolder, position)
            else -> {
                if (isCustomItemType(position)) {
                    bindCustomItemViewHolder(holder as CustomItemViewHolder, position)
                } else {
                    bindItemViewHolder(holder as ItemViewHolder<VB>, position)
                }
            }
        }
    }

    override fun getItemCount(): Int = if (isInfiniteLoopMode()) Int.MAX_VALUE else getItemSpecialCount()

    override fun getItemViewType(position: Int): Int {
        if (isInfiniteLoopMode()) {
            val listPosition = getItemListPosition(position)
            val itemData = list.getOrNull(listPosition) ?: return TYPE_ITEM
            return resolveDisplayItemViewType(itemData, listPosition)
        }
        val (customItemViewType, isCustomLayout) = isCustomByPosition(position)
        return when {
            isHeaderPosition(position) -> TYPE_HEADER
            isEmptyPosition(position) -> TYPE_EMPTY
            isFooterPosition(position) -> TYPE_FOOTER
            isCustomLayout -> customItemViewType
            else -> TYPE_ITEM
        }
    }

    fun initData(list: List<T>, useDataSetChanged: Boolean) {
        updateDataSet(useDataSetChanged) {
            this.list.clear()
            this.list.addAll(list)
        }
    }

    open fun initData(list: List<T>) {
        initData(list, false)
    }

    open fun insertData(data: T) {
        insertData(data, false)
    }

    open fun insertData(data: T, position: Int) {
        insertData(data, position, false)
    }

    open fun removeData(position: Int): T? {
        return removeData(position, false)
    }

    open fun removeAll() {
        removeAll(false)
    }

    open fun addData(list: List<T>) {
        addData(list, false)
    }

    open fun addData(list: List<T>, position: Int) {
        addData(list, position, false)
    }

    open fun updateData(data: T, position: Int) {
        updateData(data, position, false)
    }

    open fun moveData(fromPosition: Int, toPosition: Int) {
        moveData(fromPosition, toPosition, false)
    }

    fun insertData(data: T, useDataSetChanged: Boolean) {
        insertData(data, list.size, useDataSetChanged)
    }

    fun insertData(data: T, position: Int, useDataSetChanged: Boolean) {
        if (position !in 0..list.size) {
            throw IndexOutOfBoundsException("insert position out of bounds: position=$position, list.size=${list.size}")
        }
        updateDataSet(useDataSetChanged) {
            list.add(position, data)
        }
    }

    fun removeData(position: Int, useDataSetChanged: Boolean): T? {
        if (position !in list.indices) return null
        var removedData: T? = null
        updateDataSet(useDataSetChanged) {
            removedData = list.removeAt(position)
        }
        return removedData
    }

    fun removeAll(useDataSetChanged: Boolean) {
        if (list.isEmpty() && !isEmptyView()) return
        updateDataSet(useDataSetChanged) {
            list.clear()
        }
    }

    fun addData(list: List<T>, useDataSetChanged: Boolean) {
        addData(list, this.list.size, useDataSetChanged)
    }

    fun addData(list: List<T>, position: Int, useDataSetChanged: Boolean) {
        if (list.isEmpty()) return
        if (position !in 0..this.list.size) {
            throw IndexOutOfBoundsException("Add position out of bounds: position=$position, list.size=${this.list.size}")
        }
        updateDataSet(useDataSetChanged) {
            this.list.addAll(position, list)
        }
    }

    fun updateData(data: T, position: Int, useDataSetChanged: Boolean) {
        if (position !in this.list.indices) return
        updateDataSet(useDataSetChanged) {
            this.list[position] = data
        }
    }

    fun moveData(fromPosition: Int, toPosition: Int, useDataSetChanged: Boolean) {
        if (fromPosition !in this.list.indices || toPosition !in this.list.indices) return
        if (fromPosition == toPosition) return
        updateDataSet(useDataSetChanged) {
            Collections.swap(this.list, fromPosition, toPosition)
        }
    }

    fun addHeaderView(headerView: IHeaderSupport<*>) {
        updateStructure {
            this.headerView = headerView
        }
    }

    fun addFooterView(footerView: IFooterSupport<*>) {
        updateStructure {
            this.footerView = footerView
        }
    }

    fun addCustomItem(customItemView: CustomItemView) {
        updateStructure {
            customItems.add(customItemView)
        }
    }

    fun addCustomItems(block: MutableList<CustomItemView>.() -> Unit) {
        updateStructure {
            block(customItems)
        }
    }

    open fun getCustomItemViewType(data: T, position: Int): Int = -1

    @SuppressLint("NotifyDataSetChanged")
    fun notifyGlobalRefresh() {
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemClickListener(block: (view: View, data: T, position: Int, listPosition: Int) -> Unit) {
        this.onItemClickListener = object : OnItemClickListener<T> {
            override fun onItemClick(view: View, data: T, position: Int, listPosition: Int) {
                block(view, data, position, listPosition)
            }
        }
    }

    fun isCustomItemType(position: Int): Boolean {
        return !noCustomization.contains(getItemViewType(position))
    }

    //获取尾布局的Position，如果没有实现尾布局或者当前不显示，则返回-1
    fun getFooterPosition(): Int {
        return if (hasDisplayedFooter() && !isInfiniteLoopMode()) getDisplayedHeaderCount() + list.size else -1
    }

    fun getHeaderPosition(): Int {
        return if (hasDisplayedHeader() && !isInfiniteLoopMode()) 0 else -1
    }

    fun getEmptyPosition(): Int {
        return if (isEmptyView() && !isInfiniteLoopMode()) getDisplayedHeaderCount() else -1
    }

    fun getItemRealCount() = if (itemRealCount == itemCount) -1 else itemRealCount

    fun setOccupySpace(
        isHeaderFull: Boolean = true,
        isFooterFull: Boolean = true,
        isCustomFull: Boolean = true,
        isEmptyFull: Boolean
    ) {
        this.isHeaderFull = isHeaderFull
        this.isFooterFull = isFooterFull
        this.isCustomFull = isCustomFull
        this.isEmptyFull = isEmptyFull
    }

    fun setShowEmptyAndHeader(isSimultaneously: Boolean) {
        updateStructure {
            showEmptyAndHeader = isSimultaneously
        }
    }

    fun setOnHeaderClickListener(onHeaderClickListener: OnHeaderClickListener?) {
        this.onHeaderClickListener = onHeaderClickListener
    }

    fun setOnHeaderClickListener(block: (view: View, position: Int) -> Unit) {
        setOnHeaderClickListener(object : OnHeaderClickListener {
            override fun onHeaderClick(view: View, position: Int) {
                block.invoke(view, position)
            }
        })
    }

    fun setOnFooterClickListener(onFooterClickListener: OnFooterClickListener?) {
        this.onFooterClickListener = onFooterClickListener
    }

    fun setOnFooterClickListener(block: (view: View, position: Int) -> Unit) {
        setOnFooterClickListener(object : OnFooterClickListener {
            override fun onFooterClick(view: View, position: Int) {
                block.invoke(view, position)
            }
        })
    }

    fun setOnEmptyViewClickListener(onEmptyViewClickListener: OnEmptyViewClickListener?) {
        this.onEmptyViewClickListener = onEmptyViewClickListener
    }

    fun setOnEmptyViewClickListener(block: (view: View, position: Int) -> Unit) {
        setOnEmptyViewClickListener(object : OnEmptyViewClickListener {
            override fun onEmptyViewClick(view: View, position: Int) {
                block.invoke(view, position)
            }
        })
    }

    fun setOnCustomItemClickListener(onCustomItemClickListener: OnCustomItemClickListener<T>?) {
        this.onCustomItemClickListener = onCustomItemClickListener
    }

    fun setOnCustomItemClickListener(block: (view: View, data: T, position: Int, listPosition: Int) -> Unit) {
        this.onCustomItemClickListener = object : OnCustomItemClickListener<T> {
            override fun onCustomItemClick(view: View, data: T, position: Int, listPosition: Int) {
                block.invoke(view, data, position, listPosition)
            }
        }
    }

    class HeaderViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    class FooterViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    class EmptyViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    class CustomItemViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    class ItemViewHolder<VB : ViewBinding>(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    private fun bindEmptyViewHolder(holder: EmptyViewHolder, position: Int) {
        holder.viewBinding.apply {
            setItemFull(root, isEmptyFull)
            root.setOnClickListener { onEmptyViewClickListener?.onEmptyViewClick(it, position) }
            callBindMethod(this@AbsSpecialAdapter, this, BIND_EMPTY)
        }
    }

    private fun bindHeaderViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.viewBinding.apply {
            setItemFull(root, isHeaderFull)
            root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it, position) }
            callBindMethod(getIHeaderSupport(), this, BIND_HEAD)
        }
    }

    private fun bindFooterViewHolder(holder: FooterViewHolder, position: Int) {
        holder.viewBinding.apply {
            setItemFull(root, isFooterFull)
            root.setOnClickListener { onFooterClickListener?.onFooterClick(it, position) }
            callBindMethod(getIFooterSupport(), this, BIND_FOOTER)
        }
    }

    private fun bindItemViewHolder(holder: ItemViewHolder<VB>, position: Int) {
        val listPosition = getItemListPosition(position)
        val itemData = list.getOrNull(listPosition) ?: return
        holder.viewBinding.apply {
            root.setOnClickListener { onItemClickListener?.onItemClick(it, itemData, position, listPosition) }
            bindViewHolder(itemData, listPosition)
        }
    }

    private fun bindCustomItemViewHolder(holder: CustomItemViewHolder, position: Int) {
        val listPosition = getItemListPosition(position)
        val itemData = list.getOrNull(listPosition) ?: return
        val customItem = getCustomItemByViewType(getItemViewType(position)) ?: return
        holder.viewBinding.apply {
            setItemFull(root, isCustomFull)
            root.setOnClickListener { onCustomItemClickListener?.onCustomItemClick(it, itemData, position, listPosition) }
            callCustomBindMethod(customItem.supper, this, itemData, listPosition)
        }
    }

    private fun setItemFull(view: View, isFull: Boolean) {
        if (!isFull) return
        val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams ?: return
        layoutParams.isFullSpan = true
        view.layoutParams = layoutParams
    }

    private fun getItemListPosition(position: Int): Int {
        if (list.isEmpty()) return -1
        return if (isInfiniteLoopMode()) {
            Math.floorMod(position, list.size)
        } else {
            position - getDisplayedHeaderCount()
        }
    }

    private fun getCustomItemByViewType(viewType: Int): CustomItemView? {
        return customItems.firstOrNull { it.viewType == viewType }
    }

    private fun isFooterPosition(position: Int) = position == getFooterPosition()

    private fun isEmptyViewWithHeader() = isEmptyView() && hasDisplayedHeader()

    private fun isEmptyView() = hasEmptyViewImpl() && shouldShowEmptyView

    private fun isEmptyPosition(position: Int) = position == getEmptyPosition()

    private fun isHeaderPosition(position: Int) = position == getHeaderPosition()

    private fun isCustomByPosition(position: Int): Pair<Int, Boolean> {
        if (customItems.isEmpty() || isHeaderPosition(position) || isEmptyPosition(position) || isFooterPosition(position)) {
            return Pair(-1, false)
        }
        val listPosition = getItemListPosition(position)
        val itemData = list.getOrNull(listPosition) ?: return Pair(-1, false)
        val customItemViewType = getCustomItemViewType(itemData, listPosition)
        val isCustomLayout = when {
            customItemViewType == -1 -> {
                Log.e(TAG, "error: getCustomItemViewType method is not overridden")
                false
            }

            checkItemType.contains(customItemViewType) -> {
                Log.e(
                    TAG,
                    "error: The type of custom layout, it is not recommended to use $TYPE_HEADER, $TYPE_FOOTER, $TYPE_EMPTY these will conflict with the header, footer, and empty layout types . position:$listPosition"
                )
                false
            }

            customItemViewType == TYPE_ITEM -> false
            else -> true
        }
        return Pair(customItemViewType, isCustomLayout)
    }

    private fun hasDisplayedHeader(): Boolean {
        return hasHeaderImpl() && (!isEmptyView() || showEmptyAndHeader)
    }

    private fun hasDisplayedFooter(): Boolean {
        return hasFooterImpl() && !isEmptyView()
    }

    private fun getDisplayedHeaderCount(): Int {
        return if (hasDisplayedHeader()) 1 else 0
    }

    private fun getDisplayedFooterCount(): Int {
        return if (hasDisplayedFooter()) 1 else 0
    }

    private fun getItemSpecialCount(): Int {
        return when {
            isEmptyViewWithHeader() -> getDisplayedHeaderCount() + 1
            isEmptyView() -> 1
            else -> getDisplayedHeaderCount() + list.size + getDisplayedFooterCount()
        }
    }

    private fun isInfiniteLoopMode(): Boolean {
        return infiniteLoopEnabled &&
            list.size > 1 &&
            !isEmptyView() &&
            !hasDisplayedHeader() &&
            !hasDisplayedFooter()
    }

    private fun syncDisplayState() {
        shouldShowEmptyView = list.isEmpty()
        if (!isRealItemCountOverridden) {
            initRealItemCount()
        }
    }

    private inline fun updateDataSet(useDataSetChanged: Boolean, action: () -> Unit) {
        val oldDisplayItems = buildDisplayItems()
        action()
        syncDisplayState()
        if (useDataSetChanged || infiniteLoopEnabled) {
            notifyGlobalRefresh()
            return
        }
        dispatchDisplayDiff(oldDisplayItems, buildDisplayItems())
    }

    private inline fun updateStructure(action: () -> Unit) {
        val oldDisplayItems = buildDisplayItems()
        action()
        syncDisplayState()
        if (infiniteLoopEnabled) {
            notifyGlobalRefresh()
            return
        }
        dispatchDisplayDiff(oldDisplayItems, buildDisplayItems())
    }

    private fun dispatchDisplayDiff(
        oldDisplayItems: List<DisplayItem<T>>,
        newDisplayItems: List<DisplayItem<T>>
    ) {
        DiffUtil.calculateDiff(DisplayDiffCallback(oldDisplayItems, newDisplayItems), true)
            .dispatchUpdatesTo(this)
    }

    private fun buildDisplayItems(): List<DisplayItem<T>> {
        val displayItems = mutableListOf<DisplayItem<T>>()
        if (hasDisplayedHeader()) {
            displayItems += DisplayItem.Header
        }
        if (isEmptyView()) {
            displayItems += DisplayItem.Empty
            return displayItems
        }
        list.forEachIndexed { index, data ->
            displayItems += DisplayItem.Item(
                data = data,
                listPosition = index,
                viewType = resolveDisplayItemViewType(data, index)
            )
        }
        if (hasDisplayedFooter()) {
            displayItems += DisplayItem.Footer
        }
        return displayItems
    }

    private fun resolveDisplayItemViewType(data: T, position: Int): Int {
        if (customItems.isEmpty()) return TYPE_ITEM
        val customItemViewType = getCustomItemViewType(data, position)
        return when {
            customItemViewType == -1 -> TYPE_ITEM
            checkItemType.contains(customItemViewType) -> TYPE_ITEM
            else -> customItemViewType
        }
    }

    private fun callBindMethod(support: Any, viewBinding: ViewBinding, methodName: String) {
        try {
            val method: Method = support.javaClass.getMethod(methodName, viewBinding.javaClass)
            method.invoke(support, viewBinding)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callCustomBindMethod(
        support: ICustomItemSupper<*, *>,
        viewBinding: ViewBinding,
        data: T,
        customPosition: Int,
        methodName: String = BIND_CUSTOM
    ) {
        try {
            val method = support.javaClass.declaredMethods.first { it.name == methodName }
            method.invoke(support, viewBinding, data, customPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasHeaderImpl() = this is IHeaderSupport<*> || headerView != null

    fun hasFooterImpl() = this is IFooterSupport<*> || footerView != null

    fun hasEmptyViewImpl() = this is IEmptyViewSupport<*>

    fun hasCustomViewImpl() = customItems.isNotEmpty() || this is ICustomItemSupper<*, *>

    private fun getIHeaderSupport() =
        if (headerView != null) headerView as IHeaderSupport<ViewBinding> else this as IHeaderSupport<ViewBinding>

    private fun getIFooterSupport() =
        if (footerView != null) footerView as IFooterSupport<ViewBinding> else this as IFooterSupport<ViewBinding>

    private fun getIEmptyViewSupport() = this as IEmptyViewSupport<ViewBinding>

    private sealed class DisplayItem<out T> {
        object Header : DisplayItem<Nothing>()
        object Footer : DisplayItem<Nothing>()
        object Empty : DisplayItem<Nothing>()
        data class Item<T>(val data: T, val listPosition: Int, val viewType: Int) : DisplayItem<T>()
    }

    private class DisplayDiffCallback<T>(
        private val oldItems: List<DisplayItem<T>>,
        private val newItems: List<DisplayItem<T>>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return when {
                oldItem is DisplayItem.Header && newItem is DisplayItem.Header -> true
                oldItem is DisplayItem.Footer && newItem is DisplayItem.Footer -> true
                oldItem is DisplayItem.Empty && newItem is DisplayItem.Empty -> true
                oldItem is DisplayItem.Item<*> && newItem is DisplayItem.Item<*> ->
                    oldItem.viewType == newItem.viewType && oldItem.data == newItem.data

                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return when {
                oldItem is DisplayItem.Header && newItem is DisplayItem.Header -> true
                oldItem is DisplayItem.Footer && newItem is DisplayItem.Footer -> true
                oldItem is DisplayItem.Empty && newItem is DisplayItem.Empty -> true
                oldItem is DisplayItem.Item<*> && newItem is DisplayItem.Item<*> ->
                    oldItem.viewType == newItem.viewType &&
                        oldItem.data == newItem.data &&
                        oldItem.listPosition == newItem.listPosition

                else -> false
            }
        }
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
