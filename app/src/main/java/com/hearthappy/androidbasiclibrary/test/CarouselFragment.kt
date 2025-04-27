package com.hearthappy.androidbasiclibrary.test

import android.os.Bundle
import com.hearthappy.androidbasiclibrary.databinding.FragmentTestBinding
import com.hearthappy.basic.AbsBaseFragment

class CarouselFragment : AbsBaseFragment<FragmentTestBinding>() {
    override fun FragmentTestBinding.initView(savedInstanceState: Bundle?) {
        arguments?.getInt("index")?.let { //            tvTitle.text = "$it"
        }
    }

    override fun FragmentTestBinding.initData() {
    }

    override fun FragmentTestBinding.initListener() {
    }

    override fun FragmentTestBinding.initViewModelListener() {
    }

    companion object {
        fun newInstance(index: Int): CarouselFragment {
            return CarouselFragment().apply {
                arguments = Bundle().apply {
                    putInt("index", index)
                }
            }
        }
    }
}