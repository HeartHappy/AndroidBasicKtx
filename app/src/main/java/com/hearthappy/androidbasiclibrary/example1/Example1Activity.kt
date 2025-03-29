package com.hearthappy.androidbasiclibrary.example1

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.androidbasiclibrary.MainViewModel
import com.hearthappy.androidbasiclibrary.databinding.ActivityExample1Binding
import com.hearthappy.androidbasiclibrary.databinding.ItemFooterBinding
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.addFastListener
import com.hearthappy.base.ext.addLastListener
import com.hearthappy.base.ext.findFooterViewBinding
import com.hearthappy.base.ext.getFirstCompletelyVisiblePosition
import com.hearthappy.base.ext.smoothScroller
import com.hearthappy.base.interfaces.OnCustomItemClickListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener
import com.hearthappy.base.interfaces.OnItemClickListener

class Example1Activity : AbsBaseActivity<ActivityExample1Binding>() {
    private lateinit var viewModel: MainViewModel
    private lateinit var example1Adapter: Example1Adapter


    override fun ActivityExample1Binding.initViewModelListener() {
        viewModel.ld.observe(this@Example1Activity) {
            example1Adapter.initData(it) //            mainAdapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example1Activity)), 7)//4,9,12
        }
    }

    override fun ActivityExample1Binding.initView() {
        viewModel = getViewModel(MainViewModel::class.java)
        rvList.layoutManager = LinearLayoutManager(this@Example1Activity)
        example1Adapter = Example1Adapter()
        rvList.adapter = example1Adapter
    }

    override fun ActivityExample1Binding.initListener() {
        example1Adapter.setOnFooterClickListener(object : OnFooterClickListener {
            override fun onFooterClick(view: View, position: Int) {
                Toast.makeText(this@Example1Activity, "我是尾部", Toast.LENGTH_SHORT).show()
            }
        })
        example1Adapter.setOnItemClickListener(object : OnItemClickListener<String> {
            override fun onItemClick(view: View, data: String, position: Int, listPosition: Int) {
                Toast.makeText(this@Example1Activity, "我是Item:$data,position:$listPosition", Toast.LENGTH_SHORT).show()
            }
        })
        example1Adapter.setOnHeaderClickListener(object : OnHeaderClickListener {
            override fun onHeaderClick(view: View, position: Int) {
                Toast.makeText(this@Example1Activity, "我是头部", Toast.LENGTH_SHORT).show()
            }
        })

        example1Adapter.setOnCustomItemClickListener(object : OnCustomItemClickListener {
            override fun onInsetItemClick(view: View, position: Int, customPosition: Int) {
                Toast.makeText(this@Example1Activity, "我是自定义布局:position:$position,customPosition:$customPosition", Toast.LENGTH_SHORT).show()
            }
        })


        btnInit.setOnClickListener { viewModel.ld.value?.let { it1 -> example1Adapter.initData(it1) } }
        btnInset.setOnClickListener { example1Adapter.insertData("插入数据:${example1Adapter.list.size}") }
        btnInsetTo0.setOnClickListener { example1Adapter.insertData("插入到0数据:${example1Adapter.list.size}", 0) }
        btnMove.setOnClickListener { example1Adapter.moveData(0, 8) }
        btnAdd.setOnClickListener { viewModel.ld.value?.let { it1 -> example1Adapter.addData(it1) } }
        btnAddTo0.setOnClickListener { viewModel.ld.value?.let { it1 -> example1Adapter.addData(it1, 0) } }
        btnRemove.setOnClickListener { example1Adapter.removeData(example1Adapter.list.size - 1) }
        btnRemoveAll.setOnClickListener { example1Adapter.removeAll() }
        btnInsetLayout.setOnClickListener {
            btnInsetLayout.isActivated = !btnInsetLayout.isActivated
            if (btnInsetLayout.isActivated) { //                mainAdapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example1Activity)/*, CustomItemImpl2(this@Example1Activity), CustomItemImpl3(this@Example1Activity)*/), 3/*, 7, 9*/)//4,9,12
                example1Adapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example1Activity), CustomItemImpl2(this@Example1Activity), CustomItemImpl3(this@Example1Activity)), 3, 7, 9) //4,9,12
            } else { //                mainAdapter.removeAllCustomItemLayout()
                example1Adapter.removeCustomItemLayout(3, 7, 9) //                mainAdapter.addCustomItemLayout(listOf(CustomItemImpl3(this@Example1Activity)), 10)
                //                mainAdapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example1Activity),CustomItemImpl2(this@Example1Activity),CustomItemImpl3(this@Example1Activity)),3,7,9)//4,9,1
            }
        }
        var count = 1
        rvList.addLastListener(50) {
            if (count < 3) {
                if (it) viewModel.ld.value?.let { it1 -> example1Adapter.addData(it1) }
                count++
            } else {
                rvList.findFooterViewBinding<ItemFooterBinding> {
                    tvFooter.text = "没有更多数据了"
                    tvFooter.postDelayed({
                        val visiblePosition = rvList.getFirstCompletelyVisiblePosition()
                        rvList.smoothScroller(visiblePosition - 1)
                    }, 1000)
                }
            }
        }
        rvList.addFastListener {
            if (it) {
                viewModel.ld.value?.let { it1 -> example1Adapter.initData(it1) } //                Toast.makeText(this@Example1Activity,"itemCount:${mainAdapter.itemCount},inset:${mainAdapter.customTransformMap.size},${mainAdapter.customItemLayouts.size}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun ActivityExample1Binding.initData() {
        viewModel.getListData()
    }


}