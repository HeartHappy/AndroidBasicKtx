package com.hearthappy.basic.interfaces

import androidx.viewbinding.ViewBinding

/**
 * Created Date: 3/8/25
 * @author ChenRui
 * ClassDescription：特殊适配器的扩展接口
 */

//// 下拉刷新布局接口
//interface IRefreshSupport<RB : ViewBinding> {
//    fun RB.bindRefreshViewHolder()
//}

// 头部布局接口
interface IHeaderSupport<HB : ViewBinding> {
    fun HB.bindHeaderViewHolder()
}

// 尾部布局接口
interface IFooterSupport<FB : ViewBinding> {
    fun FB.bindFooterViewHolder()
}

// 空视图布局接口
interface IEmptyViewSupport<EB : ViewBinding> {
    fun EB.bindEmptyViewHolder()
}

//插入自定义布局
interface ICustomItemSupper<CB : ViewBinding,T> {
    fun CB.bindCustomViewHolder(data:T,position: Int)
}