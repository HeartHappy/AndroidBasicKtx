package com.hearthappy.androidbasiclibrary.example1

import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemHeaderBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemListBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemRefreshBinding
import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.interfaces.IEmptyViewSupport
import com.hearthappy.basic.interfaces.IFooterSupport
import com.hearthappy.basic.interfaces.IHeaderSupport
import com.hearthappy.basic.interfaces.IRefreshSupport

/**
 * Created Date: 2025/3/8
 * @author ChenRui
 * ClassDescription：头、尾布局， 则AbsSpecialAdapter<ViewBinding类型,数据类型>()
 */
class Example1Adapter : AbsSpecialAdapter<ItemListBinding, String>(), IRefreshSupport<ItemRefreshBinding>, IHeaderSupport<ItemHeaderBinding>, IFooterSupport<ItemFooterBinding>, IEmptyViewSupport<ItemEmptyViewBinding> {

    override fun ItemListBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.text = data.plus("listPosition:$position")
    }

    override fun ItemHeaderBinding.bindHeaderViewHolder() {
        tvHeader.text = "头布局"
    }


    override fun ItemFooterBinding.bindFooterViewHolder() {
        tvFooter.text = "继续上拉，加载更多..."
    }

    override fun ItemEmptyViewBinding.bindEmptyViewHolder() {
        ivEmptyDefault.setImageResource(R.mipmap.nd_default)
    }

    override fun ItemRefreshBinding.bindRefreshViewHolder() {
        tvRefresh.text = "下拉刷新..."
    }
}