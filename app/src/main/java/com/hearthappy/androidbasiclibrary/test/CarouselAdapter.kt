package com.hearthappy.androidbasiclibrary.test

import com.hearthappy.androidbasiclibrary.databinding.FragmentTestBinding
import com.hearthappy.basic.ext.loadUrl
import com.hearthappy.basic.widget.CarouselView

class CarouselAdapter : CarouselView.AbsCarouselAdapter<FragmentTestBinding, String>() {
    override fun FragmentTestBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.loadUrl(data)
    }
}