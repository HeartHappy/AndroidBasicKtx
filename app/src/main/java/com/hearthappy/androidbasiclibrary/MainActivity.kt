package com.hearthappy.androidbasiclibrary

import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.androidbasiclibrary.example1.Example1Activity
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.findViewCoordinates

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
        ngv.initData(listOf(url/*, url, url, url, url, url, url, url, url, url*/))
    }
}