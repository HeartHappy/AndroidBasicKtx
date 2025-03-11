package com.hearthappy.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.interfaces.OnItemClickListener
import java.util.Collections


/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 10/11/24
 * @Describe 万用适配器
 */
abstract class AbsBaseAdapter<VB : ViewBinding, T>(var list: MutableList<T> = mutableListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    internal var onItemClickListener: OnItemClickListener<T>? = null

    abstract fun initViewBinding(parent: ViewGroup, viewType: Int): VB


    abstract fun VB.bindViewHolder(data: T, position: Int)

    open fun initData(list: List<T>) {
        if (list.isEmpty()) return
        notifyItemRangeRemoved(0, this.list.size)
        this.list.clear()
        this.list.addAll(list)
        notifyItemRangeChanged(0, list.size)
    }

    open fun insertData(data: T) {
        this.list.add(data)
        notifyItemRangeChanged(list.size - 1, list.size)
    }

    open fun insertData(data: T, position: Int) {
        this.list.add(position, data)
        notifyItemRangeChanged(position, list.size)
    }

    open fun removeData(position: Int): T? {
        if (position >= 0 && position < list.size) {
            val removeAt = this.list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size - position)
            return removeAt
        }
        return null
    }

    open fun removeAll() {
        val size = list.size
        if (size > 0) {
            list.clear()
            notifyItemRangeRemoved(0, size)
        }
    }

    open fun addData(list: List<T>) {
        val oldPosition = this.list.size
        if (list.isEmpty()) return
        this.list.addAll(list)
        notifyItemRangeChanged(oldPosition, this.list.size)
    }

    open fun addData(list: List<T>, position: Int) {
        val oldPosition = this.list.size
        if (list.isEmpty()) return
        this.list.addAll(position, list)
        notifyItemRangeChanged(oldPosition, this.list.size)
    }

    open fun updateData(data: T, position: Int) {
        list[position] = data
        notifyItemChanged(position)
    }

    open fun moveData(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        Collections.swap(list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        notifyItemRangeChanged(fromPosition, toPosition)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ViewHolder(initViewBinding(parent, viewType))

    override fun getItemCount(): Int = list.size

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbsBaseAdapter<*, *>.ViewHolder -> {
                holder.viewBinding.root.setOnClickListener { v -> onItemClickListener?.onItemClick(v, list[position], position) }
                (holder.viewBinding as VB).bindViewHolder(list[position], position)
            }

            else -> Unit
        }
    }
}