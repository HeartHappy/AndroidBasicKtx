package com.hearthappy.basic

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.basic.interfaces.OnItemClickListener
import java.lang.reflect.InvocationTargetException
import java.util.Collections


/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 10/11/24
 * @Describe 基础适配器
 */
abstract class AbsBaseAdapter<VB : ViewBinding, T>(
    var list: MutableList<T> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var itemRealCount = -1 //真实数量，例如：无限列表，实际显示数量5个，进行轮播时使用

    inner class ViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    internal var onItemClickListener: OnItemClickListener<T>? = null

    open fun initViewBinding(parent: ViewGroup, viewType: Int): VB? = null


    abstract fun VB.bindViewHolder(data: T, position: Int)


    fun initRealItemCount() {
        itemRealCount = list.size
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
        if (list.isEmpty()) return
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

    @SuppressLint("NotifyDataSetChanged")
    fun notifyGlobalRefresh() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ViewHolder(initViewBinding(parent, viewType) ?: byViewBinding(LayoutInflater.from(parent.context), parent))

    override fun getItemCount(): Int = list.size

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AbsBaseAdapter<*, *>.ViewHolder -> {
                holder.viewBinding.apply {
                    if (list.size == itemCount) {
                        root.setOnClickListener { v -> onItemClickListener?.onItemClick(v, list[position], position, position) }
                        (this as VB).bindViewHolder(list[position], position)
                    } else if (list.size != itemCount && itemRealCount != -1) {
                        val realPosition = position % itemRealCount
                        root.setOnClickListener { v -> onItemClickListener?.onItemClick(v, list[realPosition], realPosition, realPosition) }
                        (this as VB).bindViewHolder(list[realPosition], realPosition)
                    }
                }
            }

            else -> Unit
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun byViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB {
        val type = javaClass.genericSuperclass
        if (type is java.lang.reflect.ParameterizedType) {
            val clazz = type.actualTypeArguments[0] as Class<VB>
            try {
                val inflateMethod = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
                return inflateMethod.invoke(null, inflater, container, false) as VB
            } catch (e: IllegalAccessException) {
                Log.e("AbsBaseAdapter", "IllegalAccessException: ${e.message}", e)
            } catch (e: InvocationTargetException) {
                Log.e("AbsBaseAdapter", "InvocationTargetException: ${e.message}", e)
            }
        }
        throw IllegalArgumentException("Failed to get ViewBinding instance.")
    }

    private fun syncDisplayState() {
        initRealItemCount()
    }

    private inline fun updateDataSet(useDataSetChanged: Boolean, action: () -> Unit) {
        val oldDisplayItems = buildDisplayItems()
        action()
        syncDisplayState()
        if (useDataSetChanged) {
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
        return list.mapIndexed { index, data ->
            DisplayItem.Item(
                data = data,
                listPosition = index,
                viewType = getItemViewType(index)
            )
        }
    }

    private sealed class DisplayItem<out T> {
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
                oldItem is DisplayItem.Item<*> && newItem is DisplayItem.Item<*> ->
                    oldItem.viewType == newItem.viewType && oldItem.data == newItem.data

                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return when {
                oldItem is DisplayItem.Item<*> && newItem is DisplayItem.Item<*> ->
                    oldItem.viewType == newItem.viewType &&
                        oldItem.data == newItem.data &&
                        oldItem.listPosition == newItem.listPosition

                else -> false
            }
        }
    }
}
