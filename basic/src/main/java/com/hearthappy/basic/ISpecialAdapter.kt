package com.hearthappy.basic

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hearthappy.basic.interfaces.OnItemClickListener

abstract class ISpecialAdapter<VB : ViewBinding, T>(var list: MutableList<T> = mutableListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var itemRealCount = -1 //真实数量，例如：无限列表，实际显示数量5个，进行轮播时使用
    internal var onItemClickListener: OnItemClickListener<T>? = null

    open fun initViewBinding(parent: ViewGroup, viewType: Int): VB? = null
    abstract fun VB.bindViewHolder(data: T, position: Int)
    fun initRealItemCount() {
        itemRealCount = list.size
    }
//
//    abstract fun initData(list: List<T>, useDataSetChanged: Boolean = false)
//
//    abstract fun insertData(data: T, useDataSetChanged: Boolean = false)
//
//    abstract fun insertData(data: T, position: Int, useDataSetChanged: Boolean = false)
//
//    abstract fun removeData(position: Int, useDataSetChanged: Boolean = false): T?
//
//    abstract fun removeAll(useDataSetChanged: Boolean = false)
//
//    abstract fun addData(list: List<T>, useDataSetChanged: Boolean = false)
//
//    abstract fun addData(list: List<T>, position: Int, useDataSetChanged: Boolean = false)
//
//    abstract fun updateData(data: T, position: Int, useDataSetChanged: Boolean = false)
//
//    abstract fun moveData(fromPosition: Int, toPosition: Int, useDataSetChanged: Boolean = false)

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>?) {
        this.onItemClickListener = onItemClickListener
    }


}