package com.hearthappy.androidbasiclibrary.example2

import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemGridListBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemHeaderBinding
import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.interfaces.IEmptyViewSupport
import com.hearthappy.basic.interfaces.IHeaderSupport

/**
 * Created Date: 2025/3/8
 * @author ChenRui
 * ClassDescription：头、尾布局， 则AbsSpecialAdapter<ViewBinding类型,数据类型>()
 */
class Example2Adapter : AbsSpecialAdapter<ItemGridListBinding, String>(),/* IFooterSupport<ItemFooterBinding>,*/ IHeaderSupport<ItemHeaderBinding>, IEmptyViewSupport<ItemEmptyViewBinding> {

    override fun ItemGridListBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.text = data.plus("\nlistPosition:$position")
    }

    override fun ItemHeaderBinding.bindHeaderViewHolder() {
        tvHeader.text = "我是头部"
    }


    //    override fun ItemFooterBinding.bindFooterViewHolder() {
    //        tvFooter.text = "我是尾部"
    //    }

    override fun ItemEmptyViewBinding.bindEmptyViewHolder() {
        ivEmptyDefault.setImageResource(R.mipmap.nd_default)
    }

    //    override fun ItemHeaderBinding.bindHeaderViewHolder() {
    //        tvHeader.text = "头布局"
    //    }
    //
    //    override fun ItemRefreshBinding.bindRefreshViewHolder() {
    //        tvRefresh.text = "下拉刷新..."
    //    }


}