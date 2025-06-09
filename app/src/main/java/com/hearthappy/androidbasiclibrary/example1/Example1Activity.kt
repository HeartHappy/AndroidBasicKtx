package com.hearthappy.androidbasiclibrary.example1

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.androidbasiclibrary.MainViewModel
import com.hearthappy.androidbasiclibrary.databinding.ActivityExample1Binding
import com.hearthappy.androidbasiclibrary.databinding.PopSettingsBinding
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.popupWindow
import com.hearthappy.basic.ext.showLocation
import com.hearthappy.basic.interfaces.OnCustomItemClickListener
import com.hearthappy.basic.interfaces.OnFooterClickListener
import com.hearthappy.basic.interfaces.OnHeaderClickListener
import com.hearthappy.basic.interfaces.OnItemClickListener

class Example1Activity : AbsBaseActivity<ActivityExample1Binding>() {
    private lateinit var viewModel: MainViewModel
    private lateinit var example1Adapter: Example1Adapter


    override fun ActivityExample1Binding.initViewModelListener() {
        viewModel.ld.observe(this@Example1Activity) {
            example1Adapter.initData(it)
        }
    }

    override fun ActivityExample1Binding.initView() {
        viewModel = getViewModel(MainViewModel::class.java)
        rvList.layoutManager = LinearLayoutManager(this@Example1Activity)
        example1Adapter = Example1Adapter(this@Example1Activity)
        rvList.adapter = example1Adapter //屏幕宽高
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
                startActivity(Example2Activity::class.java)
            }
        })

        example1Adapter.setOnCustomItemClickListener(object : OnCustomItemClickListener<String> {

            override fun onCustomItemClick(view: View, data: String, position: Int, listPosition: Int) {
                Toast.makeText(this@Example1Activity, "我是自定义布局:position:$listPosition", Toast.LENGTH_SHORT).show()
            }
        })
        btnSettings.setOnClickListener {
            popupWindow(viewBinding = PopSettingsBinding.inflate(layoutInflater), viewEventListener = {
                it.apply {
                    btnInit.setOnClickListener {
                        viewModel.ld.value?.let { it1 ->
                            example1Adapter.initData(it1)
                        }.also { dismiss() }
                    }
                    btnInset.setOnClickListener {
                        example1Adapter.insertData("插入数据:${example1Adapter.list.size}").also { dismiss() }
                    }
                    btnInsetTo0.setOnClickListener {
                        example1Adapter.insertData("插入到0数据:${example1Adapter.list.size}", 0).also { dismiss() }
                    }
                    btnMove.setOnClickListener {
                        example1Adapter.moveData(0, 8).also { dismiss() }
                    }
                    btnAdd.setOnClickListener {
                        viewModel.ld.value?.let { it1 ->
                            example1Adapter.addData(it1)
                        }.also { dismiss() }
                    }
                    btnAddTo0.setOnClickListener {
                        viewModel.ld.value?.let { it1 ->
                            example1Adapter.addData(it1, 0)
                        }.also { dismiss() }
                    }
                    btnRemove.setOnClickListener {
                        example1Adapter.removeData(example1Adapter.list.size - 1).also { dismiss() }
                    }
                    btnRemoveAll.setOnClickListener {
                        example1Adapter.removeAll().also { dismiss() }
                    }
                }
            }).showLocation(root)
        }
    }

    override fun ActivityExample1Binding.initData() {
        viewModel.getListData()
    }


}