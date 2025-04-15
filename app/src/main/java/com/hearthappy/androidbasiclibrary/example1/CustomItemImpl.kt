package com.hearthappy.androidbasiclibrary.example1

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ItemInsetViewBinding
import com.hearthappy.base.ext.dp2px
import com.hearthappy.base.interfaces.ICustomItemSupper

class CustomItemImpl(val context: Context):ICustomItemSupper<ItemInsetViewBinding> {

    override fun ItemInsetViewBinding.bindCustomViewHolder(position:Int) {
        root.layoutParams.apply {
            width=ConstraintLayout.LayoutParams.MATCH_PARENT
            height=30.dp2px()
        }
        ivEmptyDefault.text="我是插入的布局"
        ivEmptyDefault.setTextColor(ContextCompat.getColor(context, R.color.blue))
    }
}