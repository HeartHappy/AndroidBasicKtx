package com.hearthappy.androidbasiclibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemHeaderBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemListBinding
import com.hearthappy.base.AbsSpecialAdapter

/**
 * Created Date: 2025/3/8
 * @author ChenRui
 * ClassDescription：头、尾布局没有， 则AbsSpecialAdapter<ItemListBinding,Nothing,Nothing,String>()
 */
class MainAdapter(private val context: Context) : AbsSpecialAdapter<ItemListBinding, ItemHeaderBinding, ItemFooterBinding,ItemEmptyViewBinding, String>(isLoadHeader = true, isLoadFooter = true) {
    override fun initViewBinding(parent: ViewGroup, viewType: Int): ItemListBinding {
        return ItemListBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun initHeaderBinding(parent: ViewGroup, viewType: Int): ItemHeaderBinding {
        return ItemHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun initFooterBinding(parent: ViewGroup, viewType: Int): ItemFooterBinding {
        return ItemFooterBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun initEmptyBinding(parent: ViewGroup, viewType: Int): ItemEmptyViewBinding {
        return ItemEmptyViewBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemHeaderBinding.bindHeaderViewHolder() {
        tvHeader.text = "我是头部"
    }

    override fun ItemFooterBinding.bindFooterViewHolder() {
        tvFooter.text = "我是尾部"
    }

    override fun ItemListBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.text = data
    }

    override fun ItemEmptyViewBinding.bindEmptyViewHolder() {

    }
}