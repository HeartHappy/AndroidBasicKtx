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
import java.lang.reflect.Method


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
            TYPE_HEADER -> if (hasHeaderImpl()) return HeaderViewHolder((this as IHeaderSupport<ViewBinding>).initHeaderBinding(parent, viewType))
            TYPE_EMPTY -> if (hasEmptyViewImpl()) return EmptyViewHolder((this as IEmptyViewSupport<ViewBinding>).initEmptyBinding(parent, viewType))
            TYPE_FOOTER -> if (hasFooterImpl()) return FooterViewHolder((this as IFooterSupport<ViewBinding>).initFooterBinding(parent, viewType))
            TYPE_INSET_ITEM -> if (hasInsetItemImpl()) return InsetItemViewHolder((this as IInsetItemSupper<ViewBinding>).initInsetItemBinding(parent, viewType))
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
                val realPosition = getItemRealPosition(position)
                Log.d(TAG, "onBindViewHolder: position:$position,realPosition:$realPosition")
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
        var realPosition = position
        if (hasHeaderImpl()) realPosition--
        if (hasInsetItemImpl()) {
            val insetIndex = (this as IInsetItemSupper<*>).insetItemPosition()
            Log.d(TAG, "getRealPosition: realPosition$realPosition,insetIndex:$insetIndex")
            if (realPosition > insetIndex - 1) realPosition--
        }
        return if (realPosition >= 0) realPosition else 0
    }

    private fun getItemRealPosition(position: Int): Int {
        var realPosition = position // 处理头部布局的偏移量
        if (hasHeaderImpl()) {
            realPosition--
        }

        // 获取插入布局的位置
        val insetItemPosition = getIInsetItemSupper().insetItemPosition()
        if (insetItemPosition != -1) {
            if (position >= insetItemPosition + (if (hasHeaderImpl()) 1 else 0)) {
                realPosition--
                Log.d(TAG, "getItemRealPosition: $realPosition")
            }
        }

        // 处理尾部布局的情况，如果当前是尾部布局，直接返回 -1 表示无效索引
        if (hasFooterImpl() && position == itemCount - 1) {
            realPosition--
        }
        return realPosition
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            hasEmptyViewImpl() && shouldShowEmptyView -> TYPE_EMPTY
            hasHeaderImpl() && position == 0 -> TYPE_HEADER
            hasFooterImpl() && position == headerOffset + list.size + insetItemOffset -> TYPE_FOOTER
            hasInsetItemImpl() && getIInsetItemSupper().insetItemPosition() == position -> TYPE_INSET_ITEM
            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        headerOffset = if (hasHeaderImpl()) 1 else 0
        footerOffset = if (hasFooterImpl()) 1 else 0
        insetItemPosition = getIInsetItemSupper().insetItemPosition()
        insetItemOffset = if (hasInsetItemImpl() && insetItemPosition != -1 && list.size > insetItemOffset) 1 else 0
        return if (shouldShowEmptyView && hasEmptyViewImpl()) {
            1
        } else {
            headerOffset + list.size + insetItemOffset + footerOffset
        }
    }


    override fun initData(list: List<T>) {
        val headerOffset = if (hasHeaderImpl()) 1 else 0
        notifyItemRangeRemoved(headerOffset, this.list.size)
        this.list.clear()
        this.list.addAll(list)
        shouldShowEmptyView = list.isEmpty()
        notifyItemRangeChanged(0, if (shouldShowEmptyView) 1 else list.size + headerOffset + insetItemOffset + footerOffset)
    }

    override fun insertData(data: T) {
        val position = this.list.size
        this.list.add(data)
        shouldShowEmptyView = false
        Log.d(TAG, "insertData: ${list.size}")
        Log.d(TAG, "insertData 刷新: ${position + headerOffset + insetItemOffset}")
        notifyItemInserted(position + headerOffset + insetItemOffset)
    }

    override fun insertData(data: T, position: Int) {
        this.list.add(position, data)
        shouldShowEmptyView = false
        if (insetItemPosition != -1) {
            if (position >= insetItemPosition) {
                notifyItemInserted(position + headerOffset + insetItemOffset)
                return
            }
        }
        notifyItemInserted(position + headerOffset + insetItemOffset)
    }

    override fun removeData(position: Int): T? {
        val virtualPosition = position + headerOffset + if (position + headerOffset >= insetItemPosition) insetItemOffset else 0
        if (position >= 0 && position < list.size) {
            val removedItem = list.removeAt(position)
            shouldShowEmptyView = list.isEmpty()
            Log.d(TAG, "removeData: $virtualPosition,${list}")
            notifyItemRemoved(virtualPosition) //            notifyItemRangeChanged(virtualPosition, list.size + headerOffset + insetItemOffset)
            return removedItem
        }
        return null
    }


    override fun removeAll() {
        val size = list.size
        if (size > 0) {
            list.clear()
            val headerOffset = if (hasHeaderImpl()) 1 else 0
            val footerOffset = if (hasFooterImpl()) 1 else 0 // 如果有头部和底部布局，移除头尾布局
            if (hasFooterImpl()) notifyItemRemoved(footerOffset)
            notifyItemRangeRemoved(headerOffset, size)
            if (hasHeaderImpl()) notifyItemRemoved(0)
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
        val headerOffset = if (hasHeaderImpl()) 1 else 0
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

    override fun updateData(position: Int, data: T) {
        list[position] = data
        shouldShowEmptyView = false
        notifyItemChanged(position + if (hasHeaderImpl()) 1 else 0)
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

    companion object {
        private const val TAG = "AbsSpecialAdapter"
        const val TYPE_HEADER: Int = 0
        const val TYPE_ITEM: Int = 1
        const val TYPE_EMPTY: Int = 2
        const val TYPE_FOOTER: Int = 3
        const val TYPE_INSET_ITEM = 4
    }
}