package com.hearthappy.androidbasiclibrary.example2

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.androidbasiclibrary.MainViewModel
import com.hearthappy.androidbasiclibrary.databinding.ActivityExample2Binding
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.bindSpecialAdapter
import com.hearthappy.base.ext.createActivityCircularReveal
import com.hearthappy.base.ext.disappearCircularReveal
import com.hearthappy.base.interfaces.OnCustomItemClickListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener
import com.hearthappy.base.interfaces.OnItemClickListener

class Example2Activity : AbsBaseActivity<ActivityExample2Binding>() {
    private lateinit var viewModel: MainViewModel
    private lateinit var example2Adapter: Example2Adapter


    override fun ActivityExample2Binding.initViewModelListener() {
        viewModel.ld.observe(this@Example2Activity) {
            example2Adapter.initData(it) //            mainAdapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example2Activity)), 7)//4,9,12
            example2Adapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example2Activity)), 4) //4,9,12
        }
    }

    override fun ActivityExample2Binding.initView() {
        val coordinates = getCarryCoordinates()
        createActivityCircularReveal(500, coordinates.first.toInt(), coordinates.second.toInt())
        viewModel = getViewModel(MainViewModel::class.java)
        example2Adapter = Example2Adapter()
        val gridLayoutManager = GridLayoutManager(this@Example2Activity, 2, LinearLayoutManager.VERTICAL, false)
        rvList.layoutManager = gridLayoutManager.apply { bindSpecialAdapter(example2Adapter) }
        rvList.adapter = example2Adapter
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
                disappearCircularReveal(500)
                Toast.makeText(this@Example2Activity, "我是头部", Toast.LENGTH_SHORT).show()
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