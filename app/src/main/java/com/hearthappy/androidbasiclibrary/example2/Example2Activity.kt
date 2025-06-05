package com.hearthappy.androidbasiclibrary.example2

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.hearthappy.androidbasiclibrary.MainViewModel
import com.hearthappy.androidbasiclibrary.databinding.ActivityExample2Binding
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl2
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl3
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.createActivityCircularReveal
import com.hearthappy.basic.ext.disappearCircularReveal
import com.hearthappy.basic.ext.setOccupySpace
import com.hearthappy.basic.interfaces.OnCustomItemClickListener
import com.hearthappy.basic.interfaces.OnFooterClickListener
import com.hearthappy.basic.interfaces.OnHeaderClickListener
import com.hearthappy.basic.interfaces.OnItemClickListener

class Example2Activity : AbsBaseActivity<ActivityExample2Binding>() {
    private lateinit var viewModel: MainViewModel
    private lateinit var example2Adapter: Example2Adapter

    override fun ActivityExample2Binding.initViewModelListener() {
        viewModel.ld.observe(this@Example2Activity) {
            example2Adapter.initData(it)
            example2Adapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example2Activity), CustomItemImpl2(this@Example2Activity), CustomItemImpl3(this@Example2Activity)), 4, 8, 12) //4,9,12
        }
    }

    override fun ActivityExample2Binding.initView() {
//        val coordinates = getCarryCoordinates()
//        createActivityCircularReveal(500, coordinates.first.toInt(), coordinates.second.toInt())
        viewModel = getViewModel(MainViewModel::class.java)
        example2Adapter = Example2Adapter() //        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val gridLayoutManager = GridLayoutManager(this@Example2Activity, 2, GridLayoutManager.VERTICAL, false)
        rvList.layoutManager = gridLayoutManager
        rvList.adapter = example2Adapter
        rvList.setOccupySpace(isEmptyFull = true)
        rvList.itemAnimator = null
    }

    override fun ActivityExample2Binding.initListener() {
        example2Adapter.setOnFooterClickListener(object : OnFooterClickListener {
            override fun onFooterClick(view: View, position: Int) {
                Toast.makeText(this@Example2Activity, "我是尾部", Toast.LENGTH_SHORT).show()

            }
        })
        example2Adapter.setOnItemClickListener(object : OnItemClickListener<String> {
            override fun onItemClick(view: View, data: String, position: Int, listPosition: Int) {
                Toast.makeText(this@Example2Activity, "我是Item:$data,position:$listPosition", Toast.LENGTH_SHORT).show()

            }
        })
        example2Adapter.setOnHeaderClickListener(object : OnHeaderClickListener {
            override fun onHeaderClick(view: View, position: Int) {
//                disappearCircularReveal(500)
                Toast.makeText(this@Example2Activity, "我是头部,更新列表", Toast.LENGTH_SHORT).show()
                viewModel.ld.value?.let { example2Adapter.initData(it.drop(10),false) }
            }
        })

        example2Adapter.setOnCustomItemClickListener(object : OnCustomItemClickListener {
            override fun onInsetItemClick(view: View, position: Int, customPosition: Int) {
                Toast.makeText(this@Example2Activity, "我是自定义布局:position:$position,customPosition:$customPosition", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun ActivityExample2Binding.initData() {
        viewModel.getListData()
    }


}