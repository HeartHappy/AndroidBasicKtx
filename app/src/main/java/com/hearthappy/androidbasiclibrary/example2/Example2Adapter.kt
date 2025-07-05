package com.hearthappy.androidbasiclibrary.example2

import android.content.Context
import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemGridListBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemHeaderBinding
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl2
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl3
import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.interfaces.IEmptyViewSupport
import com.hearthappy.basic.interfaces.IFooterSupport
import com.hearthappy.basic.interfaces.IHeaderSupport
import com.hearthappy.basic.model.CustomItemView

/**
 * Created Date: 2025/3/8
 * @author ChenRui
 * ClassDescription：头、尾布局， 则AbsSpecialAdapter<ViewBinding类型,数据类型>()
 */
class Example2Adapter(private val context : Context) : AbsSpecialAdapter<ItemGridListBinding, String>(), IFooterSupport<ItemFooterBinding>, IHeaderSupport<ItemHeaderBinding>, IEmptyViewSupport<ItemEmptyViewBinding> {
    init {
        addCustomItems {
            add(CustomItemView(TYPE_CUSTOM1, CustomItemImpl(context)))
            add(CustomItemView(TYPE_CUSTOM2, CustomItemImpl2(context)))
            add(CustomItemView(TYPE_CUSTOM3, CustomItemImpl3(context)))
        }
    }

    override fun ItemGridListBinding.bindViewHolder(data : String, position : Int) {
        tvTitle.text = data.plus("\nlistPosition:$position")
    }

    override fun ItemHeaderBinding.bindHeaderViewHolder() {
        tvHeader.text = "我是头部"
    }


        override fun ItemFooterBinding.bindFooterViewHolder() {
            tvFooter.text = "我是尾部"
        }

    override fun ItemEmptyViewBinding.bindEmptyViewHolder() {
        ivEmptyDefault.setImageResource(R.mipmap.nd_default)
    }


    override fun getCustomItemViewType(data : String, position : Int) : Int {
        return when (position) {
            5 -> TYPE_CUSTOM1
            8 -> TYPE_CUSTOM2
            13 -> TYPE_CUSTOM3
            else -> TYPE_ITEM
        }
    }

    companion object {
        const val TYPE_CUSTOM1 = 5
        const val TYPE_CUSTOM2 = 6
        const val TYPE_CUSTOM3 = 7
    }


}