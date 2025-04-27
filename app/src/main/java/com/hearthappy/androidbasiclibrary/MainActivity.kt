package com.hearthappy.androidbasiclibrary

import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.androidbasiclibrary.example1.Example1Activity
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.androidbasiclibrary.test.CarouselAdapter
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.findViewCoordinates
import com.hearthappy.basic.widget.PagerTransformer

class MainActivity : AbsBaseActivity<ActivityMainBinding>() {
    override fun ActivityMainBinding.initData() {
    }

    override fun ActivityMainBinding.initListener() {
        btnExample1.setOnClickListener {
            startActivity(Example1Activity::class.java)
        }
        btnExample2.setOnClickListener {
            val coordinates = it.findViewCoordinates()
            startActivityCarryCoordinates(Example2Activity::class.java, coordinates)
        }
    }


    override fun ActivityMainBinding.initViewModelListener() {

    }

    override fun ActivityMainBinding.initView() {
        val url = "http://dongting10.oss-cn-beijing.aliyuncs.com/admin/png/5aa3cfcea145a90d7de7b73f68c35e52.png"
        val carouselAdapter = CarouselAdapter()
        vp.setAdapter(carouselAdapter)
        vp.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL)
        vp.setScrollInterval(3000, 1000)
        vp.setPageTransformer(PagerTransformer(PagerTransformer.AnimType.TRANSLATE))
        carouselAdapter.initData(listOf(url, url, url, url, url, url))
        vp.addListener(onPageSelected = {
            Log.d("TAG", "onPageSelected: $it")
        })
    }
}