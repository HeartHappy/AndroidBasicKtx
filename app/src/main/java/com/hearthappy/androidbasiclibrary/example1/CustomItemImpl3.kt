package com.hearthappy.androidbasiclibrary.example1

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemInsetViewBinding
import com.hearthappy.basic.interfaces.ICustomItemSupper

class CustomItemImpl3(val context: Context):ICustomItemSupper<ItemInsetViewBinding> {

    override fun ItemInsetViewBinding.bindCustomViewHolder(position:Int) {
        ivEmptyDefault.text="我是插入的布局3"
        ivEmptyDefault.setTextColor(ContextCompat.getColor(context, R.color.green))
        Log.d("TAG", "bindCustomViewHolder: $position")
    }
}
