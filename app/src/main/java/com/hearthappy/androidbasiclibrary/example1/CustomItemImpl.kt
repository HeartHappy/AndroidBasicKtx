package com.hearthappy.androidbasiclibrary.example1

import android.content.Context
import androidx.core.content.ContextCompat
import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemInsetViewBinding
import com.hearthappy.basic.interfaces.ICustomItemSupper

class CustomItemImpl(val context: Context):ICustomItemSupper<ItemInsetViewBinding> {

    override fun ItemInsetViewBinding.bindCustomViewHolder(position:Int) {
        ivEmptyDefault.text="我是插入的布局"
        ivEmptyDefault.setTextColor(ContextCompat.getColor(context, R.color.blue))
    }
}