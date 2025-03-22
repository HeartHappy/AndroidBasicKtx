package com.hearthappy.androidbasiclibrary

import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.androidbasiclibrary.example1.Example1Activity
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.base.AbsBaseActivity

class MainActivity : AbsBaseActivity<ActivityMainBinding>() {

    override fun ActivityMainBinding.initData() {
        viewBinding.apply { }
    }

    override fun ActivityMainBinding.initListener() {
        btnExample1.setOnClickListener {
            startActivity(Example1Activity::class.java)
        }
        btnExample2.setOnClickListener {
            startActivity(Example2Activity::class.java)
        }

    }

    override fun ActivityMainBinding.initViewModelListener() {

    }

    override fun ActivityMainBinding.initView() {

    }
}