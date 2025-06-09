package com.hearthappy.basic

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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
abstract class AbsBaseAdapter<VB : ViewBinding, T>(var list: MutableList<T> = mutableListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var itemRealCount = -1 //真实数量，例如：无限列表，实际显示数量5个，进行轮播时使用

    inner class ViewHolder(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root)

    internal var onItemClickListener: OnItemClickListener<T>? = null

    open fun initViewBinding(parent: ViewGroup, viewType: Int): VB? = null


    abstract fun VB.bindViewHolder(data: T, position: Int)


    fun initRealItemCount() {
        itemRealCount = list.size
    }

    open fun initData(list: List<T>) {
        if (list.isEmpty()) return
        if (this.list.isNotEmpty()) {
            val size = this.list.size
            this.list.clear()
            notifyItemRangeRemoved(0, size)
        }
        this.list.addAll(list)
        notifyItemRangeChanged(0, list.size)
        initRealItemCount()
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
}