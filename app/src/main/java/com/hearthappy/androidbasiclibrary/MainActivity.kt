package com.hearthappy.androidbasiclibrary

import android.view.Gravity
import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemEmptyViewBinding
import com.hearthappy.androidbasiclibrary.example1.Example1Activity
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.findViewCoordinates
import com.hearthappy.basic.ext.popupWindow
import com.hearthappy.basic.ext.showAtBottom

class MainActivity : AbsBaseActivity<ActivityMainBinding>() {
    override fun ActivityMainBinding.initData() {
    }

    override fun ActivityMainBinding.initListener() {
        btnExample1.setOnClickListener { //            startActivity(Example1Activity::class.java)
            popupWindow(ItemEmptyViewBinding.inflate(layoutInflater), width = 200, height = 200, viewEventListener = {
                it.ivEmptyDefault.setOnClickListener { //                    dismiss()
                    //                    startActivity(Example1Activity::class.java)
                    //                    finish()

                    popupWindow(key = "empty", viewBinding = ItemEmptyViewBinding.inflate(layoutInflater), width = 200, height = 200, viewEventListener = {vb->
                        vb.ivEmptyDefault.setOnClickListener{
                                                startActivity(Example1Activity::class.java)
                                                finish()
                        }
                    }).showAtBottom(root)
                }
            }).showAtLocation(root, Gravity.CENTER, 0, 0)
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