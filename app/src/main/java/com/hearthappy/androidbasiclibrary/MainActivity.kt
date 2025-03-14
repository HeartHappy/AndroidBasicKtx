package com.hearthappy.androidbasiclibrary

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.base.ext.addFastListener
import com.hearthappy.base.ext.addLastListener
import com.hearthappy.base.interfaces.OnFooterClickListener
import com.hearthappy.base.interfaces.OnHeaderClickListener
import com.hearthappy.base.interfaces.OnItemClickListener

class MainActivity : AbsBaseActivity<ActivityMainBinding>() {

    private lateinit var viewModel: MainViewModel
    private lateinit var mainAdapter: MainAdapter
    override fun initViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun ActivityMainBinding.initData() {
        viewModel.getListData()
    }

    override fun ActivityMainBinding.initListener() {
        mainAdapter.setFooterClickListener(object : OnFooterClickListener {
            override fun onFooterClick(view: View) {
                Toast.makeText(this@MainActivity, "我是尾部", Toast.LENGTH_SHORT).show()
            }
        })
        mainAdapter.setOnItemClickListener(object : OnItemClickListener<String> {
            override fun onItemClick(view: View, data: String, position: Int) {
                Toast.makeText(this@MainActivity, "我是Item:$data,position:$position", Toast.LENGTH_SHORT).show()
            }
        })
        mainAdapter.setOnHeaderClickListener(object : OnHeaderClickListener {
            override fun onHeaderClick(view: View) {
                Toast.makeText(this@MainActivity, "我是头部", Toast.LENGTH_SHORT).show()
            }
        })


        btnInit.setOnClickListener { viewModel.ld.value?.let { it1 -> mainAdapter.initData(it1) } }
        btnInset.setOnClickListener { mainAdapter.insertData("插入数据:${mainAdapter.list.size}") }
        btnInsetTo0.setOnClickListener { mainAdapter.insertData("插入到0数据:${mainAdapter.list.size}", 0) }
        btnMove.setOnClickListener { mainAdapter.moveData(0, 8) }
        btnAdd.setOnClickListener { viewModel.ld.value?.let { it1 -> mainAdapter.addData(it1) } }
        btnAddTo0.setOnClickListener { viewModel.ld.value?.let { it1 -> mainAdapter.addData(it1, 0) } }
        btnRemove.setOnClickListener { mainAdapter.removeData(mainAdapter.list.size - 1) }
        btnRemoveAll.setOnClickListener { mainAdapter.removeAll() }
        btnInsetLayout.setOnClickListener {
            btnInsetLayout.isActivated = !btnInsetLayout.isActivated
            if (btnInsetLayout.isActivated) {
                mainAdapter.setInsetItemLayout(listOf(InsetItemImpl(this@MainActivity),InsetItemImpl(this@MainActivity),InsetItemImpl(this@MainActivity)),3,7,9)
            } else {
                mainAdapter.setInsetItemLayout(emptyList())
            }
        }

        rvList.addLastListener {
            if (it) viewModel.ld.value?.let { it1 -> mainAdapter.addData(it1) }
            Toast.makeText(this@MainActivity, "加载更多", Toast.LENGTH_SHORT).show()
        }
        rvList.addFastListener {
            if (it) {
                viewModel.ld.value?.let { it1 -> mainAdapter.initData(it1) }
                Toast.makeText(this@MainActivity, "刷新数据", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun ActivityMainBinding.initViewModelListener() {
        viewModel.ld.observe(this@MainActivity) {
//            mainAdapter.setInsetItemLayout(listOf(InsetItemImpl(this@MainActivity)),3)
            mainAdapter.initData(it)

            Log.d("TAG", "initViewModelListener:${mainAdapter.itemCount} ")
        }
    }

    override fun ActivityMainBinding.initView() {
        viewModel = getViewModel(MainViewModel::class.java)
        rvList.layoutManager = LinearLayoutManager(this@MainActivity)
        mainAdapter = MainAdapter(this@MainActivity)
        rvList.adapter = mainAdapter
    }
}