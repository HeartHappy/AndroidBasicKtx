package com.hearthappy.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.interfaces.OnEmptyViewClickListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener


/**
 * Created Date: 2025/3/7
 * @author ChenRui
 * ClassDescription：特殊适配，支持头尾布局
 */
abstract class AbsSpecialAdapter<VB : ViewBinding, HB : ViewBinding, FB : ViewBinding, EB : ViewBinding, T>(val isLoadHeader: Boolean = false, val isLoadFooter: Boolean = false, var isEmptyViewVisible: Boolean = false) : AbsBaseAdapter<VB, T>() {

    open fun initHeaderBinding(parent: ViewGroup, viewType: Int): HB? = null
    open fun initFooterBinding(parent: ViewGroup, viewType: Int): FB? = null
    open fun initEmptyBinding(parent: ViewGroup, viewType: Int): EB? = null
    open fun HB.bindHeaderViewHolder() {}
    open fun FB.bindFooterViewHolder() {}
    open fun EB.bindEmptyViewHolder() {}

    private var onHeaderClickListener: OnHeaderClickListener? = null
    private var onFooterClickListener: OnFooterClickListener? = null
    private var onEmptyViewClickListener: OnEmptyViewClickListener? = null
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
        return when (viewType) {
            TYPE_HEADER -> initHeaderBinding(parent, viewType)?.run { HeaderViewHolder(this) } ?: throw RuntimeException("Override the initHeaderBinding method or isLoadHeader is set to false")
            TYPE_ITEM -> ItemViewHolder(initViewBinding(parent, viewType))
            TYPE_EMPTY -> initEmptyBinding(parent, viewType)?.run { EmptyViewHolder(this) } ?: throw RuntimeException("Override the initEmptyBinding method or isEmptyViewVisible is set to false")
            TYPE_FOOTER -> initFooterBinding(parent, viewType)?.run { FooterViewHolder(this) } ?: throw RuntimeException("Override the initFooterBinding method or isLoadFooter is set to false")
            else -> ItemViewHolder(initViewBinding(parent, viewType))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val realPosition = getRealPosition(position)
        when (holder) {
            is AbsSpecialAdapter<*, *, *, *, *>.HeaderViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it) }
                (holder.viewBinding as HB).bindHeaderViewHolder()
            }

            is AbsSpecialAdapter<*, *, *, *, *>.ItemViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onItemClickListener?.onItemClick(it, list[realPosition], realPosition) }
                (holder.viewBinding as VB).bindViewHolder(list[realPosition], realPosition)
            }

            is AbsSpecialAdapter<*, *, *, *, *>.EmptyViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onEmptyViewClickListener?.onEmptyViewClick(it) }
                (holder.viewBinding as EB).bindEmptyViewHolder()
            }

            is AbsSpecialAdapter<*, *, *, *, *>.FooterViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onFooterClickListener?.onFooterClick(it) }
                (holder.viewBinding as FB).bindFooterViewHolder()
            }

            else -> Unit
        }
    }

    private fun getRealPosition(position: Int): Int {
        return position - (if (isLoadHeader) 1 else 0)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isEmptyViewVisible -> TYPE_EMPTY
            isLoadHeader && position == 0 -> TYPE_HEADER
            isLoadFooter && position == list.size + 1 -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        val hfCount = (if (isLoadHeader) 1 else 0) + (if (isLoadFooter) 1 else 0)
        return if (isEmptyViewVisible) {
            1
        } else {
            hfCount + list.size
        }

    }

    fun showEmptyView() {
        isEmptyViewVisible = true
        notifyItemChanged(0)
    }

    override fun initData(list: List<T>) {
        if (list.isEmpty()) return
        val headerOffset = if (isLoadHeader) 1 else 0
        notifyItemRangeRemoved(headerOffset, this.list.size)
        this.list.clear()
        this.list.addAll(list)
        notifyItemRangeChanged(headerOffset, list.size)
    }

    override fun insertData(data: T) {
        val position = this.list.size
        this.list.add(data)
        val headerOffset = if (isLoadHeader) 1 else 0
        notifyItemInserted(position + headerOffset)
    }

    override fun insertData(data: T, position: Int) {
        this.list.add(position, data)
        val headerOffset = if (isLoadHeader) 1 else 0
        notifyItemInserted(position + headerOffset)
    }

    override fun removeData(position: Int): T? {
        val adjustedPosition = position - if (isLoadHeader) 1 else 0
        if (adjustedPosition >= 0 && adjustedPosition < list.size) {
            val removedItem = this.list.removeAt(adjustedPosition)
            notifyItemRemoved(adjustedPosition + if (isLoadHeader) 1 else 0)
            notifyItemRangeChanged(adjustedPosition + if (isLoadHeader) 1 else 0, list.size - adjustedPosition)
            return removedItem
        }
        return null
    }

    override fun removeAll() {
        val size = list.size
        if (size > 0) {
            list.clear()
            notifyItemRangeRemoved(if (isLoadHeader) 1 else 0, size)
        }
    }

    override fun addData(list: List<T>) {
        val oldPosition = this.list.size
        if (list.isEmpty()) return
        this.list.addAll(list)
        val headerOffset = if (isLoadHeader) 1 else 0
        notifyItemRangeChanged(oldPosition + headerOffset, this.list.size - oldPosition)
    }

    override fun addData(list: List<T>, position: Int) {
        if (list.isEmpty()) return
        this.list.addAll(position, list)
        val headerOffset = if (isLoadHeader) 1 else 0
        if (position == 0) {
            notifyItemRangeInserted(headerOffset, list.size)
        } else {
            notifyItemRangeChanged(position + headerOffset, this.list.size)
        }
    }

    override fun updateData(position: Int, data: T) {
        list[position] = data
        notifyItemChanged(position + if (isLoadHeader) 1 else 0)
    }


    private inner class HeaderViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class FooterViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class EmptyViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class ItemViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    companion object {
        const val TYPE_HEADER: Int = 0
        const val TYPE_ITEM: Int = 1
        const val TYPE_EMPTY: Int = 2
        const val TYPE_FOOTER: Int = 3
    }
}