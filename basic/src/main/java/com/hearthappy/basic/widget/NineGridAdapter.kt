package com.hearthappy.basic.widget

import android.util.Log
import com.hearthappy.androidbasicktx.databinding.ItemNineGridBinding
import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.ext.loadUrl


class NineGridAdapter(val singleWidth: Int, val singleHeight: Int, val proportional: Int) : AbsSpecialAdapter<ItemNineGridBinding, String>() {
    override fun ItemNineGridBinding.bindViewHolder(data: String, position: Int) {
        if (itemCount == 1) {
            root.layoutParams.apply {
                width = singleWidth // 自定义宽度
                height = singleHeight // 自定义高度
            }
            Log.d(TAG, "bindViewHolder=1: $singleWidth,$singleHeight")
        } else if (itemCount > 1) {
            root.layoutParams.apply {
                width = proportional // 自定义宽度
                height = proportional // 自定义高度
            }
            Log.d(TAG, "bindViewHolder>1: $proportional")
        }

        ivPicture.loadUrl(data)
    }

    companion object {
        private const val TAG = "NineGridAdapter"
    }
}