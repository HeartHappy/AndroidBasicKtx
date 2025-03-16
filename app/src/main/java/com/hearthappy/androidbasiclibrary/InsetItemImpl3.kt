package com.hearthappy.androidbasiclibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.hearthappy.androidbasiclibrary.databinding.ItemInsetViewBinding
import com.hearthappy.base.interfaces.IInsetItemSupper

class InsetItemImpl3(val context: Context):IInsetItemSupper<ItemInsetViewBinding> {
    override fun initInsetItemBinding(parent: ViewGroup, viewType: Int): ItemInsetViewBinding {
        return ItemInsetViewBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun ItemInsetViewBinding.bindInsetViewHolder() {
        ivEmptyDefault.text="我是插入的布局3"
        ivEmptyDefault.setTextColor(ContextCompat.getColor(context,R.color.green))
        ivEmptyDefault.setOnClickListener {
            Toast.makeText(context, "我是插入布局3", Toast.LENGTH_SHORT).show()
        }
    }
}
