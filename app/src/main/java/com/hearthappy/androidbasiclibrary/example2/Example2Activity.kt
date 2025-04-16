package com.hearthappy.androidbasiclibrary.example2

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hearthappy.androidbasiclibrary.MainViewModel
import com.hearthappy.androidbasiclibrary.R
import com.hearthappy.androidbasiclibrary.databinding.ActivityExample2Binding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.androidbasiclibrary.databinding.ItemRefreshBinding
import com.hearthappy.androidbasiclibrary.example1.CustomItemImpl
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.createActivityCircularReveal
import com.hearthappy.basic.ext.disappearCircularReveal
import com.hearthappy.basic.interfaces.OnCustomItemClickListener
import com.hearthappy.basic.interfaces.OnFooterClickListener
import com.hearthappy.basic.interfaces.OnHeaderClickListener
import com.hearthappy.basic.interfaces.OnItemClickListener

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
        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //        val gridLayoutManager = GridLayoutManager(this@Example2Activity, 2, GridLayoutManager.VERTICAL, false)
        rvList.layoutManager = gridLayoutManager
        rvList.adapter = example2Adapter
        rvList.setOccupySpace()
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
        var count = 1
        rvList.addOnLoadMoreListener<ItemFooterBinding> {
            if (count < 3) {
                viewModel.ld.value?.let { it1 -> example2Adapter.addData(it1) }
                count++
            } else {
                tvFooter.text = "没有更多数据了"
            }
        }

        rvList.addOnRefreshListener<ItemRefreshBinding>(onRefreshProgress = { progress ->
            tvRefresh.text = getString(R.string.pull_down_to_refresh)
            circularProgress.progress = progress.toInt()
            if (progress >= 100f) {
                tvRefresh.text = "松开完成刷新"
            }
        }, onRefreshFinish = {
            viewModel.ld.value?.let { it1 ->
                example2Adapter.initData(it1, true)
            }
        })
    }

    override fun ActivityExample2Binding.initData() {
        viewModel.getListData()
    }


}