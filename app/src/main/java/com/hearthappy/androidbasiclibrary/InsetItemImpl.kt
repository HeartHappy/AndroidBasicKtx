package com.hearthappy.androidbasiclibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.hearthappy.androidbasiclibrary.databinding.ItemInsetViewBinding
import com.hearthappy.base.interfaces.IInsetItemSupper

class InsetItemImpl(val context: Context):IInsetItemSupper<ItemInsetViewBinding> {
    override fun initInsetItemBinding(parent: ViewGroup, viewType: Int): ItemInsetViewBinding {
        return ItemInsetViewBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemInsetViewBinding.bindInsetViewHolder() {
        ivEmptyDefault.text="我是插入的布局，哈哈"
        ivEmptyDefault.setOnClickListener {
            Toast.makeText(context, "我是插入布局", Toast.LENGTH_SHORT).show()
        }
    }
}