package com.hearthappy.androidbasiclibrary.example2

import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hearthappy.androidbasiclibrary.MainViewModel
import com.hearthappy.androidbasiclibrary.databinding.ActivityExample2Binding
import com.hearthappy.androidbasiclibrary.databinding.PopSettingsBinding
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.popupWindow
import com.hearthappy.basic.ext.setOccupySpace
import com.hearthappy.basic.ext.showAtBottom

class Example2Activity : AbsBaseActivity<ActivityExample2Binding>() {
    private lateinit var viewModel: MainViewModel
    private lateinit var example2Adapter: Example2Adapter

    override fun ActivityExample2Binding.initViewModelListener() {
        viewModel.ld.observe(this@Example2Activity) {
            example2Adapter.initData(it) //            example2Adapter.setCustomItemLayout(listOf(CustomItemImpl(this@Example2Activity), CustomItemImpl2(this@Example2Activity), CustomItemImpl3(this@Example2Activity)), 4, 8, 12) //4,9,12
        }
    }

    override fun ActivityExample2Binding.initView() { //        val coordinates = getCarryCoordinates()
        //        createActivityCircularReveal(500, coordinates.first.toInt(), coordinates.second.toInt())
        viewModel = getViewModel(MainViewModel::class.java)
        example2Adapter = Example2Adapter(this@Example2Activity)
        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //        val gridLayoutManager = GridLayoutManager(this@Example2Activity, 2, GridLayoutManager.VERTICAL, false)
        rvList.layoutManager = gridLayoutManager
        rvList.adapter = example2Adapter
        rvList.setOccupySpace(isEmptyFull = true, isCustomFull = true)
        rvList.itemAnimator = null
    }

    override fun ActivityExample2Binding.initListener() {
        //新增lambda表达式

        example2Adapter.setOnFooterClickListener { _, _ ->
            Toast.makeText(this@Example2Activity, "我是尾部", Toast.LENGTH_SHORT).show()
        }
        example2Adapter.setOnItemClickListener { _, d, _, lp ->
            Toast.makeText(this@Example2Activity, "我是Item:$d,position:$lp", Toast.LENGTH_SHORT).show()
        }
        example2Adapter.setOnHeaderClickListener { _, _ -> //            disappearCircularReveal(500)
            Toast.makeText(this@Example2Activity, "我是头部,更新列表", Toast.LENGTH_SHORT).show()
            viewModel.ld.value?.let { example2Adapter.initData(it.drop(10)) }
        }

        example2Adapter.setOnCustomItemClickListener { _, _, _, d ->
            Toast.makeText(this@Example2Activity, "我是自定义布局:position:$d", Toast.LENGTH_SHORT).show()
        }
        btnSettings.setOnClickListener {
            popupWindow(viewBinding = PopSettingsBinding.inflate(layoutInflater), viewEventListener = {
                it.apply {
                    btnInit.setOnClickListener {
                        viewModel.ld.value?.let { it1 ->
                            example2Adapter.initData(it1)
                        }.also { dismiss() }
                    }
                    btnInset.setOnClickListener {
                        example2Adapter.insertData("插入数据:${example2Adapter.list.size}").also { dismiss() }
                    }
                    btnInsetTo0.setOnClickListener {
                        example2Adapter.insertData("插入到0数据:${example2Adapter.list.size}", 0).also { dismiss() }
                    }
                    btnMove.setOnClickListener {
                        example2Adapter.moveData(0, 8).also { dismiss() }
                    }
                    btnAdd.setOnClickListener {
                        viewModel.ld.value?.let { it1 ->
                            example2Adapter.addData(it1)
                        }.also { dismiss() }
                    }
                    btnAddTo0.setOnClickListener {
                        viewModel.ld.value?.let { it1 ->
                            example2Adapter.addData(it1, 0)
                        }.also { dismiss() }
                    }
                    btnRemove.setOnClickListener {
                        example2Adapter.removeData(0).also { dismiss() }
                    }
                    btnRemoveAll.setOnClickListener {
                        example2Adapter.removeAll().also { dismiss() }
                    }
                }
            }).showAtBottom(root)
        }
    }

    override fun ActivityExample2Binding.initData() {
        viewModel.getListData()
    }


}