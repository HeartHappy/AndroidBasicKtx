package com.hearthappy.base

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
    private var headerOffset = 0
    private var footerOffset = 0
    private var insetItemOffset = 0
    private var insetItemPosition = -1

    fun setOnHeaderClickListener(onHeaderClickListener: OnHeaderClickListener?) {
        this.onHeaderClickListener = onHeaderClickListener
    }

    fun setFooterClickListener(onFooterClickListener: OnFooterClickListener?) {
        this.onFooterClickListener = onFooterClickListener
    }

    fun setEmptyViewClickListener(onEmptyViewClickListener: OnEmptyViewClickListener?) {
        this.onEmptyViewClickListener = onEmptyViewClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> if (hasHeaderImpl()) return HeaderViewHolder(getIHeaderSupport().initHeaderBinding(parent, viewType))
            TYPE_EMPTY -> if (hasEmptyViewImpl()) return EmptyViewHolder(getIEmptyViewSupport().initEmptyBinding(parent, viewType))
            TYPE_FOOTER -> if (hasFooterImpl()) return FooterViewHolder(getIFooterSupport().initFooterBinding(parent, viewType))
            TYPE_INSET_ITEM -> if (hasInsetItemImpl()) return InsetItemViewHolder(getIInsetItemSupper().initInsetItemBinding(parent, viewType))
        }
        return ItemViewHolder(initViewBinding(parent, viewType))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbsSpecialAdapter<*, *>.HeaderViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it) }
                callBindMethod(this, holder.viewBinding, "bindHeaderViewHolder")
            }

            is AbsSpecialAdapter<*, *>.ItemViewHolder -> {
                val realPosition = getRealPosition(position)
                holder.viewBinding.root.setOnClickListener { onItemClickListener?.onItemClick(it, list[realPosition], realPosition) }
                (holder.viewBinding as VB).bindViewHolder(list[realPosition], realPosition)
            }

            is AbsSpecialAdapter<*, *>.EmptyViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onEmptyViewClickListener?.onEmptyViewClick(it) }
                callBindMethod(this, holder.viewBinding, "bindEmptyViewHolder")
            }

            is AbsSpecialAdapter<*, *>.FooterViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onFooterClickListener?.onFooterClick(it) }
                callBindMethod(this, holder.viewBinding, "bindFooterViewHolder")
            }

            is AbsSpecialAdapter<*, *>.InsetItemViewHolder -> { //                holder.viewBinding.root.setOnClickListener { onItemClickListener?.onItemClick(it, list[realPosition], realPosition) }
                callBindMethod(this, holder.viewBinding, "bindInsetViewHolder")
            }

            else -> Unit
        }
    }


    private fun getRealPosition(position: Int): Int {
        return position - (if (hasHeaderImpl()) 1 else 0) - if (hasInsetItemImpl() && position > insetItemPosition) insetItemOffset else 0
    }

    private fun getVirtualPosition(realPosition: Int): Int {
        return realPosition + headerOffset + if (realPosition >= insetItemPosition) +insetItemOffset else 0
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            hasEmptyViewImpl() && shouldShowEmptyView -> TYPE_EMPTY
            hasHeaderImpl() && position == 0 -> TYPE_HEADER
            hasFooterImpl() && position == headerOffset + list.size + insetItemOffset -> TYPE_FOOTER
            hasInsetItemImpl() && getIInsetItemSupper().insetItemPosition().convertInsetItemPosition() == position - headerOffset -> TYPE_INSET_ITEM
            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        headerOffset = if (hasHeaderImpl()) 1 else 0
        footerOffset = if (hasFooterImpl()) 1 else 0
        insetItemPosition = if (hasInsetItemImpl()) getIInsetItemSupper().insetItemPosition().convertInsetItemPosition() else -1
        insetItemOffset = if (hasInsetItemImpl() && insetItemPosition != -1) 1 else 0
        return if (shouldShowEmptyView && hasEmptyViewImpl()) {
            1
        } else {
            headerOffset + list.size + insetItemOffset + footerOffset
        }
    }


    override fun initData(list: List<T>) {
        notifyItemRangeRemoved(headerOffset, this.list.size)
        this.list.clear()
        this.list.addAll(list)
        shouldShowEmptyView = list.isEmpty()
        notifyItemRangeChanged(0, if (shouldShowEmptyView) 1 else getItemSpecialCount())
    }

    override fun insertData(data: T) {
        val position = this.list.size
        val virtualPosition = getVirtualPosition(position)
        this.list.add(data)
        shouldShowEmptyView = false
        if (list.size == 1) { //首次插入一条时，将头布局和插入布局刷新出来
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
        if (position >= 0 && position < list.size) {
            val removedItem = list.removeAt(position)
            shouldShowEmptyView = list.isEmpty()
            notifyItemRemoved(virtualPosition)
            notifyItemRangeChanged(virtualPosition, getItemSpecialCount())
            return removedItem
        }
        return null
    }


    override fun removeAll() {
        val size = list.size
        if (size > 0) {
            list.clear()
            if (hasHeaderImpl()) notifyItemRemoved(0)
            notifyItemRangeRemoved(headerOffset, size)
            if (hasFooterImpl()) notifyItemRemoved(footerOffset)
            shouldShowEmptyView = true
            notifyItemChanged(if (hasEmptyViewImpl()) 1 else 0)
        }
    }

    override fun addData(list: List<T>) {
        val oldPosition = this.list.size
        if (list.isEmpty()) {
            shouldShowEmptyView = this.list.isEmpty()
        } else {
            shouldShowEmptyView = false
        }
        this.list.addAll(list)
        notifyItemRangeChanged(oldPosition + headerOffset, this.list.size - oldPosition)
    }

    override fun addData(list: List<T>, position: Int) {
        if (list.isEmpty()) {
            shouldShowEmptyView = this.list.isEmpty()
        } else {
            shouldShowEmptyView = false
        }
        this.list.addAll(position, list)
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        if (position == 0) {
            notifyItemRangeInserted(headerOffset, list.size)
        } else {
            notifyItemRangeChanged(position + headerOffset, this.list.size)
        }
    }

    override fun updateData(data: T, position: Int) {
        if (position >= list.size) return
        list[position] = data
        shouldShowEmptyView = false
        val virtualPosition = getVirtualPosition(position)
        notifyItemChanged(virtualPosition)
    }

    override fun moveData(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        Collections.swap(list, fromPosition, toPosition)
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

    private fun getItemSpecialCount() = list.size + headerOffset + insetItemOffset

    private fun Int.convertInsetItemPosition(): Int {
        return if (this == -1) this else {
            if (this > list.size) {
                list.size
            } else {
                this
            }
        }
    }

    companion object {
        private const val TAG = "AbsSpecialAdapter"
        const val TYPE_HEADER: Int = 0
        const val TYPE_ITEM: Int = 1
        const val TYPE_EMPTY: Int = 2
        const val TYPE_FOOTER: Int = 3
        const val TYPE_INSET_ITEM = 4
    }
}