package com.hearthappy.androidbasiclibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemHeaderBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemInsetViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemListBinding
import com.hearthappy.base.AbsSpecialAdapter
import com.hearthappy.base.interfaces.IEmptyViewSupport
import com.hearthappy.base.interfaces.IFooterSupport
import com.hearthappy.base.interfaces.IHeaderSupport
import com.hearthappy.base.interfaces.IInsetItemSupper

/**
 * Created Date: 2025/3/8
 * @author ChenRui
 * ClassDescription：头、尾布局， 则AbsSpecialAdapter<ViewBinding类型,数据类型>()
 */
class MainAdapter(private val context: Context) : AbsSpecialAdapter<ItemListBinding, String>(), IHeaderSupport<ItemHeaderBinding>, IFooterSupport<ItemFooterBinding>, IEmptyViewSupport<ItemEmptyViewBinding> {
    override fun initViewBinding(parent: ViewGroup, viewType: Int): ItemListBinding {
        return ItemListBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemListBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.text = data.plus(position)
    }

    override fun initHeaderBinding(parent: ViewGroup, viewType: Int): ItemHeaderBinding {
        return ItemHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemHeaderBinding.bindHeaderViewHolder() {
        tvHeader.text = "我是头部"
    }

    override fun initFooterBinding(parent: ViewGroup, viewType: Int): ItemFooterBinding {
        return ItemFooterBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemFooterBinding.bindFooterViewHolder() {
        tvFooter.text = "我是尾部"
    }

    override fun initEmptyBinding(parent: ViewGroup, viewType: Int): ItemEmptyViewBinding {
        return ItemEmptyViewBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemEmptyViewBinding.bindEmptyViewHolder() {
        ivEmptyDefault.setImageResource(R.mipmap.nd_default)
    }

//    override fun initInsetItemBinding(parent: ViewGroup, viewType: Int): ItemInsetViewBinding {
//        return ItemInsetViewBinding.inflate(LayoutInflater.from(context), parent, false)
//    }
//
//    override fun ItemInsetViewBinding.bindInsetViewHolder(data: String) {
//        tvEmptyDefault.text = data
//    }


}