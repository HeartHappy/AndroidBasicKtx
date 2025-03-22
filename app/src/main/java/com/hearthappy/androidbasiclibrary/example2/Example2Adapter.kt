package com.hearthappy.androidbasiclibrary.example2

import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemGridListBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemHeaderBinding
import com.hearthappy.base.AbsSpecialAdapter
import com.hearthappy.base.interfaces.IEmptyViewSupport
import com.hearthappy.base.interfaces.IFooterSupport
import com.hearthappy.base.interfaces.IHeaderSupport

/**
 * Created Date: 2025/3/8
 * @author ChenRui
 * ClassDescription：头、尾布局， 则AbsSpecialAdapter<ViewBinding类型,数据类型>()
 */
class Example2Adapter : AbsSpecialAdapter<ItemGridListBinding, String>(), IHeaderSupport<ItemHeaderBinding>, IFooterSupport<ItemFooterBinding>, IEmptyViewSupport<ItemEmptyViewBinding> {

    override fun ItemGridListBinding.bindViewHolder(data: String, position: Int) {
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


}