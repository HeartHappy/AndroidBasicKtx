package com.hearthappy.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.interfaces.IEmptyViewSupport
import com.hearthappy.base.interfaces.IFooterSupport
import com.hearthappy.base.interfaces.IHeaderSupport
import com.hearthappy.base.interfaces.OnEmptyViewClickListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener
import java.lang.reflect.Method


/**
 * Created Date: 2025/3/7
 * @author ChenRui
 * ClassDescription：特殊适配，支持头、尾、空布局SpecialAdapter<ViewBinding类型,数据类型>()
 * 根据需求实现：IHeaderSupport、IFooterSupport、IEmptyViewSupport接口
 */
@Suppress("UNCHECKED_CAST")
abstract class AbsSpecialAdapter<VB : ViewBinding,T> : AbsBaseAdapter<VB, T>() {

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
         when (viewType) {
            TYPE_HEADER -> if (hasHeaderImpl()) return HeaderViewHolder((this as IHeaderSupport<ViewBinding>).initHeaderBinding(parent, viewType))
            TYPE_EMPTY -> if (hasEmptyViewImpl())  return  EmptyViewHolder((this as IEmptyViewSupport<ViewBinding>).initEmptyBinding(parent, viewType))
            TYPE_FOOTER -> if (hasFooterImpl()) return  FooterViewHolder((this as IFooterSupport<ViewBinding>).initFooterBinding(parent, viewType))
        }
        return ItemViewHolder(initViewBinding(parent, viewType))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val realPosition = getRealPosition(position)
        when (holder) {
            is AbsSpecialAdapter<*, *>.HeaderViewHolder -> {
                holder.viewBinding.root.setOnClickListener { onHeaderClickListener?.onHeaderClick(it) }
                callBindMethod(this, holder.viewBinding, "bindHeaderViewHolder")
            }

            is AbsSpecialAdapter<*, *>.ItemViewHolder -> {
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
            else -> Unit
        }
    }

    private fun getRealPosition(position: Int): Int {
        return position - (if (hasHeaderImpl()) 1 else 0)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            list.isEmpty() && hasEmptyViewImpl() -> TYPE_EMPTY
            hasHeaderImpl() && position == 0 -> TYPE_HEADER
            hasFooterImpl() && position == list.size + 1 -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        val hfCount = (if (hasHeaderImpl()) 1 else 0) + (if (hasFooterImpl()) 1 else 0)
        return if (list.isEmpty() && hasEmptyViewImpl()) {
            1
        } else {
            hfCount + list.size
        }
    }


    override fun initData(list: List<T>) {
        if (list.isEmpty())return
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        notifyItemRangeRemoved(headerOffset, this.list.size)
        this.list.clear()
        this.list.addAll(list)
        notifyItemRangeChanged(headerOffset, list.size)
    }

    override fun insertData(data: T) {
        val position = this.list.size
        this.list.add(data)
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        notifyItemInserted(position + headerOffset)
    }

    override fun insertData(data: T, position: Int) {
        this.list.add(position, data)
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        notifyItemInserted(position + headerOffset)
    }

    override fun removeData(position: Int): T? {
        val adjustedPosition = position - if (hasHeaderImpl()) 1 else 0
        if (adjustedPosition >= 0 && adjustedPosition < list.size) {
            val removedItem = this.list.removeAt(adjustedPosition)
            notifyItemRemoved(adjustedPosition + if (hasHeaderImpl()) 1 else 0)
            notifyItemRangeChanged(adjustedPosition + if (hasHeaderImpl()) 1 else 0, list.size - adjustedPosition)
            return removedItem
        }
        return null
    }



    override fun removeAll() {
        val size = list.size
        if (size > 0) {
            list.clear()
            notifyItemRangeRemoved(if (hasHeaderImpl()) 1 else 0, size)
        }
    }

    override fun addData(list: List<T>) {
        val oldPosition = this.list.size
        if (list.isEmpty()) return
        this.list.addAll(list)
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        notifyItemRangeChanged(oldPosition + headerOffset, this.list.size - oldPosition)
    }

    override fun addData(list: List<T>, position: Int) {
        if (list.isEmpty()) return
        this.list.addAll(position, list)
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        if (position == 0) {
            notifyItemRangeInserted(headerOffset, list.size)
        } else {
            notifyItemRangeChanged(position + headerOffset, this.list.size)
        }
    }

    override fun updateData(position: Int, data: T) {
        list[position] = data
        notifyItemChanged(position + if (hasHeaderImpl()) 1 else 0)
    }

    private inner class HeaderViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class FooterViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class EmptyViewHolder(val viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root)
    private inner class ItemViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    private fun callBindMethod(support: Any, viewBinding: ViewBinding, methodName: String) {
        try {
            val method: Method = support.javaClass.getMethod(methodName, viewBinding.javaClass)
            method.invoke(support, viewBinding)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hasHeaderImpl()=this is IHeaderSupport<*>

    private fun hasFooterImpl()=this is IFooterSupport<*>

    private fun hasEmptyViewImpl()=this is IEmptyViewSupport<*>

    companion object {
        const val TYPE_HEADER: Int = 0
        const val TYPE_ITEM: Int = 1
        const val TYPE_EMPTY: Int = 2
        const val TYPE_FOOTER: Int = 3
    }
}