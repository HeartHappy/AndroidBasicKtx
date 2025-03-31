package com.hearthappy.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.ext.findInterfaceInflate
import com.hearthappy.base.interfaces.ICustomItemSupper
import com.hearthappy.base.interfaces.IEmptyViewSupport
import com.hearthappy.base.interfaces.IFooterSupport
import com.hearthappy.base.interfaces.IHeaderSupport
import com.hearthappy.base.interfaces.OnCustomItemClickListener
import com.hearthappy.base.interfaces.OnEmptyViewClickListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener
import java.lang.reflect.Method
import java.util.Collections


/**
 * Created Date: 2025/3/7
 * @author ChenRui
 * ClassDescription：特殊适配，支持头、尾、空布局，以及插入自定义布局。AbsSpecialAdapter<ViewBinding类型,数据类型>()
 * 根据需求实现：IHeaderSupport、IFooterSupport、IEmptyViewSupport接口
 */

@Suppress("UNCHECKED_CAST")
abstract class AbsSpecialAdapter<VB : ViewBinding, T> : AbsBaseAdapter<VB, T>() {

    private var shouldShowEmptyView: Boolean = false
    private var onHeaderClickListener: OnHeaderClickListener? = null
    private var onFooterClickListener: OnFooterClickListener? = null
    private var onEmptyViewClickListener: OnEmptyViewClickListener? = null
    private var onCustomItemClickListener: OnCustomItemClickListener? = null
    private var headerOffset = 0
    private var footerOffset = 0
    private var creatorCount = 0

    private var customItemPositions: MutableList<Int> = mutableListOf()
    private var customItemLayouts: MutableList<ICustomItemSupper<*>> = mutableListOf()
    private var customItemSupperMap = mutableMapOf<Int, ICustomItemSupper<*>>() //推算的索引，布局接口实现
    private var customTransformMap = mutableMapOf<Int, Int>() //推算的索引,原索引
    private lateinit var transformPositions: List<Int> //返回推算的索引集合


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> if (hasHeaderImpl()) return HeaderViewHolder(getIHeaderSupport().javaClass.findInterfaceInflate(parent, IHeaderSupport::class.java))
            TYPE_EMPTY -> if (hasEmptyViewImpl()) return EmptyViewHolder(getIEmptyViewSupport().javaClass.findInterfaceInflate(parent, IEmptyViewSupport::class.java))
            TYPE_FOOTER -> if (hasFooterImpl()) return FooterViewHolder(getIFooterSupport().javaClass.findInterfaceInflate(parent, IFooterSupport::class.java))
            TYPE_INSET_ITEM -> {
                val insetItem = customItemLayouts[creatorCount]
                creatorCount++
                if (creatorCount >= customItemLayouts.size) {
                    creatorCount--
                }
                return CustomItemViewHolder(insetItem.javaClass.findInterfaceInflate(parent, ICustomItemSupper::class.java))
            }
        }
        return ItemViewHolder(initViewBinding(parent, viewType) ?: byViewBinding(LayoutInflater.from(parent.context), parent))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbsSpecialAdapter<*, *>.EmptyViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onEmptyViewClickListener?.onEmptyViewClick(it, position) }
                callBindMethod(this, holder.viewBinding, BIND_EMPTY)
            }

            is AbsSpecialAdapter<*, *>.HeaderViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it, position) }
                callBindMethod(this, holder.viewBinding, BIND_HEAD)
            }

            is AbsSpecialAdapter<*, *>.ItemViewHolder -> {
                val listPosition = getItemListPosition(position)
                holder.viewBinding.root.setOnClickListener { onItemClickListener?.onItemClick(it, list[listPosition], position, listPosition) }
                (holder.viewBinding as VB).bindViewHolder(list[listPosition], listPosition)
            }

            is AbsSpecialAdapter<*, *>.CustomItemViewHolder -> {
                val customPosition = customTransformMap[position] ?: -1
                holder.viewBinding.root.setOnClickListener { onCustomItemClickListener?.onInsetItemClick(it, position, customPosition) }
                customItemSupperMap[position]?.let { callCustomBindMethod(it, holder.viewBinding, customPosition = customPosition) }
            }

            is AbsSpecialAdapter<*, *>.FooterViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onFooterClickListener?.onFooterClick(it, position) }
                callBindMethod(this, holder.viewBinding, BIND_FOOTER)
            }

            else -> Unit
        }
    }


    private fun getItemListPosition(position: Int): Int {
        var realPosition = position - headerOffset
        for (map in customTransformMap) {
            if (map.value > realPosition) break
            realPosition--
        }
        return realPosition
    }

    private fun getItemVirtualPosition(realPosition: Int): Int {
        var virtualPosition = realPosition + headerOffset
        for (map in customTransformMap) {
            if (map.value > realPosition) break
            virtualPosition++
        }
        return virtualPosition
    }

    /**
     * 插入index后推算显示位置，index 从 0 开始，所以要加 1 得到从 1 开始的位置
     * @param inputList List<Int>
     * @return List<Int>
     */
    private fun transformList(inputList: List<Int>): List<Int> = inputList.mapIndexed { index, value -> value + index + (if (hasHeaderImpl()) 1 else 0) }

    override fun getItemViewType(position: Int): Int { // 计算累积的插入布局偏移量
        return when {
            hasEmptyViewImpl() && shouldShowEmptyView -> TYPE_EMPTY
            hasHeaderImpl() && position == TYPE_HEADER -> TYPE_HEADER
            hasFooterImpl() && position == headerOffset + list.size + customTransformMap.size -> TYPE_FOOTER //            hasInsetItemImpl() && insetItemPosition != NOT_INSERTED && insetItemPosition.convertInsetItemPosition() == position - headerOffset -> TYPE_INSET_ITEM
            customTransformMap.isNotEmpty() && customTransformMap.keys.any { it == position } -> TYPE_INSET_ITEM
            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        headerOffset = if (hasHeaderImpl()) 1 else 0
        footerOffset = if (hasFooterImpl()) 1 else 0
        return if (shouldShowEmptyView && hasEmptyViewImpl()) 1 else getItemSpecialCount()
    }


    /**
     * 调用数据初始化，如果先前数据与更新数据不一致，则会移除先移除先前数据，在将新数据更新
     * @param list List<T>
     */
    override fun initData(list: List<T>) {
        if (this.list.isNotEmpty() || customTransformMap.isNotEmpty()) {
            removeAllCustomItemLayout()
            getItemSpecialCount().also {
                clearAll()
                notifyItemRangeRemoved(0, it)
                notifyItemRangeChanged(0, it)
            }
        }
        this.list.addAll(list)
        shouldShowEmptyView = list.isEmpty()
        notifyItemRangeChanged(0, if (shouldShowEmptyView) 1 else getItemSpecialCount())
    }

    override fun insertData(data: T) {
        val position = this.list.size
        val virtualPosition = getItemVirtualPosition(position)
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
        val virtualPosition = getItemVirtualPosition(position)
        shouldShowEmptyView = false
        notifyItemInserted(virtualPosition)
        notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
    }

    override fun removeData(position: Int): T? {
        val virtualPosition = getItemVirtualPosition(position)
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
            if (hasHeaderImpl()) notifyItemRemoved(0)
            notifyItemRangeRemoved(headerOffset, size + customTransformMap.size)
            if (hasFooterImpl()) notifyItemRemoved(footerOffset)
            shouldShowEmptyView = true
            clearAll()
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
        val virtualPosition = getItemVirtualPosition(position)
        notifyItemChanged(virtualPosition)
    }

    override fun moveData(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        Collections.swap(this.list, fromPosition, toPosition)
        val virtualFromPosition = getItemVirtualPosition(fromPosition)
        val virtualToPosition = getItemVirtualPosition(toPosition)
        notifyItemMoved(virtualFromPosition, virtualToPosition)
        notifyItemRangeChanged(virtualFromPosition, virtualToPosition)
    }

    fun getCustomPositions(): List<Int> {
        if (!::transformPositions.isInitialized) {
            transformPositions = customTransformMap.keys.toList()
        }
        return transformPositions
    }

    //获取尾部局的Position，如果没有实现尾部局，则返回-1
    fun getFooterPosition(): Int {
        return if (hasFooterImpl()) getItemSpecialCount() - 1 else -1
    }

    fun getHeaderPosition(): Int {
        return if (hasHeaderImpl()) 0 else -1
    }

    /**
     * 设置插入布局
     * @param customItemLayouts List<ICustomItemSupper<*>> 插入布局的接口实现集合
     * @param customItemPositions IntArray inset position
     * position range:P:-1：不插入 || P >=list.size：插入到item列表最后一条 || -1< P <list.size：插入到指定位置 ,传入空则不插入
     */
    fun setCustomItemLayout(customItemLayouts: List<ICustomItemSupper<*>>, vararg customItemPositions: Int) {
        if (customItemLayouts.size != customItemPositions.size) throw RuntimeException("The number of layout and insertion positions is not equal")
        if (customItemPositions.isEmpty() || customItemLayouts.isEmpty()) return
        removeAllCustomItemLayout()
        notifyCustomLayoutChanged(customItemLayouts, customItemPositions)
    }

    private fun notifyCustomLayoutChanged(customItemLayouts: List<ICustomItemSupper<*>>, customItemPositions: IntArray) { // 添加新的布局元素
        this.customItemLayouts = customItemLayouts.toMutableList() // 添加新的插入位置元素
        this.customItemPositions = customItemPositions.map { it.convertInsetItemPosition() }.toMutableList()
        val transformResult = transformList(this.customItemPositions.toList())
        this.customItemPositions.forEachIndexed { index, insetPosition ->
            val transformPosition = transformResult[index]
            customTransformMap[transformPosition] = insetPosition
            customItemSupperMap[transformPosition] = this.customItemLayouts[index]
            notifyItemInserted(transformPosition)
        }
        notifyItemRangeChanged(headerOffset, getItemSpecialCount())
    }

    fun addCustomItemLayout(insetItemLayouts: List<ICustomItemSupper<*>>, vararg insetItemPositions: Int) {
        if (insetItemLayouts.size != insetItemPositions.size) throw RuntimeException("The number of layout and insertion positions is not equal")
        if (insetItemPositions.isEmpty() || insetItemLayouts.isEmpty()) return
        val newLayouts = this.customItemLayouts.toMutableList()
        val newInsetPositions = this.customItemPositions.toMutableList()
        newLayouts.addAll(insetItemLayouts)
        newInsetPositions.addAll(insetItemPositions.toList())
        notifyCustomLayoutsChanged(newLayouts, newInsetPositions)
    }


    fun removeCustomItemLayout(vararg position: Int) {
        if (customTransformMap.isNotEmpty() && position.isNotEmpty()) {
            removeEntries(customTransformMap, position.toSet()) { notifyItemRemoved(it) }
            notifyItemRangeChanged(headerOffset, getItemSpecialCount())
            notifyCustomLayoutsChanged(customItemLayouts.toMutableList(), customTransformMap.values.map { it })
        }
    }


    fun removeAllCustomItemLayout() {
        if (customTransformMap.isNotEmpty()) {
            removeEntries(customTransformMap, customItemPositions.toSet()) { notifyItemRemoved(it) }
            notifyItemRangeChanged(headerOffset, getItemSpecialCount())
        }
    }

    /**
     *
     * @param map MutableMap<Int, Int> 需要删除的键值对: key:推算的索引，value:原索引
     * @param keysToRemove Set<Int> 原索引
     * @param block Function1<Int, Unit>
     */
    private fun removeEntries(map: MutableMap<Int, Int>, keysToRemove: Set<Int>, block: (Int) -> Unit) { // 移除指定的键
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next() //查找原索引
            if (keysToRemove.contains(entry.value)) {
                iterator.remove()
                val itemSupper = customItemSupperMap[entry.key]
                val indexOf = customItemLayouts.indexOf(itemSupper)
                if (indexOf == -1) break
                customItemLayouts.remove(itemSupper)
                customItemPositions.removeAt(indexOf) //移除推算的索引，布局接口实现
                customItemSupperMap.remove(entry.key)
                if (creatorCount > 0) {
                    creatorCount--
                }
                block(entry.key)
            }
        }
    }

    private fun notifyCustomLayoutsChanged(newLayouts: List<ICustomItemSupper<*>>, newInsetPosition: List<Int>) {
        removeAllCustomItemLayout()
        setCustomItemLayout(newLayouts, *newInsetPosition.toIntArray())
    }


    private fun clearAll() {
        if (list.isNotEmpty()) list.clear()
        if (customItemPositions.isNotEmpty()) customItemPositions.clear()
        if (customItemLayouts.isNotEmpty()) customItemLayouts.clear()
        if (customTransformMap.isNotEmpty()) customTransformMap.clear()
        if (customItemSupperMap.isNotEmpty()) customItemSupperMap.clear()
        creatorCount = 0
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

    private fun callCustomBindMethod(support: Any, viewBinding: ViewBinding, methodName: String = BIND_CUSTOM, customPosition: Int) {
        try {
            val method: Method = support.javaClass.getMethod(methodName, viewBinding.javaClass, Int::class.java)
            method.invoke(support, viewBinding, customPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasHeaderImpl() = this is IHeaderSupport<*>
    fun hasFooterImpl() = this is IFooterSupport<*>
    private fun hasEmptyViewImpl() = this is IEmptyViewSupport<*>
    private fun getIHeaderSupport() = this as IHeaderSupport<ViewBinding>
    private fun getIFooterSupport() = this as IFooterSupport<ViewBinding>
    private fun getIEmptyViewSupport() = this as IEmptyViewSupport<ViewBinding>
    private fun getItemSpecialCount(): Int = list.size + headerOffset + customTransformMap.size + footerOffset
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

    fun setOnCustomItemClickListener(onCustomItemClickListener: OnCustomItemClickListener?) {
        this.onCustomItemClickListener = onCustomItemClickListener
    }


    companion object {
        const val TYPE_HEADER: Int = 0x00
        const val TYPE_ITEM: Int = 0x01
        const val TYPE_EMPTY: Int = 0x02
        const val TYPE_FOOTER: Int = 0x03
        const val TYPE_INSET_ITEM = 0x04
        const val BIND_HEAD = "bindHeaderViewHolder"
        const val BIND_FOOTER = "bindFooterViewHolder"
        const val BIND_EMPTY = "bindEmptyViewHolder"
        const val BIND_CUSTOM = "bindCustomViewHolder"
    }

}
