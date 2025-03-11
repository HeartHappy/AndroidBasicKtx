package com.hearthappy.base.interfaces

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Created Date: 3/8/25
 * @author ChenRui
 * ClassDescription：特殊适配器的扩展接口
 */ // 头部布局接口
interface IHeaderSupport<HB : ViewBinding> {
    fun initHeaderBinding(parent: ViewGroup, viewType: Int): HB
    fun HB.bindHeaderViewHolder()
}

// 底部布局接口
interface IFooterSupport<FB : ViewBinding> {
    fun initFooterBinding(parent: ViewGroup, viewType: Int): FB
    fun FB.bindFooterViewHolder()
}

// 空视图布局接口
interface IEmptyViewSupport<EB : ViewBinding> {
    fun initEmptyBinding(parent: ViewGroup, viewType: Int): EB
    fun EB.bindEmptyViewHolder()
}

interface IInsetItemSupper<IB : ViewBinding> {
    fun initInsetItemBinding(parent: ViewGroup, viewType: Int): IB
    fun IB.bindInsetViewHolder()

    // 插入布局的Position，返回值：P:-1：不插入，P >=list.size：插入到item列表最后一条，-1< P <list.size：插入到指定位置
    fun insetItemPosition(): Int
}