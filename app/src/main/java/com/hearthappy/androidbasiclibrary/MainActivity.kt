package com.hearthappy.androidbasiclibrary

import android.util.Log
import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.androidbasiclibrary.example1.Example1Activity
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.findViewCoordinates

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
    }

}