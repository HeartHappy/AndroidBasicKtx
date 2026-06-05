package com.hearthappy.androidbasiclibrary

import android.graphics.drawable.GradientDrawable
import com.hearthappy.androidbasiclibrary.databinding.ItemMainBannerBinding
import com.hearthappy.basic.AbsSpecialAdapter

data class MainBannerItem(
    val title: String,
    val description: String,
    val backgroundColor: Int
)

class MainBannerAdapter : AbsSpecialAdapter<ItemMainBannerBinding, MainBannerItem>() {
    override fun ItemMainBannerBinding.bindViewHolder(data: MainBannerItem, position: Int) {
        val backgroundDrawable = GradientDrawable().apply {
            cornerRadius = root.resources.displayMetrics.density * 20
            setColor(data.backgroundColor)
        }
        layoutBannerCard.background = backgroundDrawable
        tvBannerTitle.text = data.title
        tvBannerSubTitle.text = data.description
    }
}
