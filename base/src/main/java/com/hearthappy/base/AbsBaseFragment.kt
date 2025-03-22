package com.hearthappy.base

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hearthappy.base.ext.findFragmentInflate


/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 10/11/24
 * @Describe 基础fragment
 */
abstract class AbsBaseFragment<VB : ViewBinding> : Fragment() {


    private var _binding: VB? = null
    protected val viewBinding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = initViewBinding(inflater, container) ?: findFragmentInflate(inflater, container)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            initView(savedInstanceState)
            initViewModelListener()
            initListener()
            initData()
        }
    }


    open fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB? = null

    abstract fun VB.initView(savedInstanceState: Bundle?)

    abstract fun VB.initData()

    abstract fun VB.initListener()

    abstract fun VB.initViewModelListener()


    fun startActivity(clazz: Class<*>) {
        startActivity(Intent(context, clazz))
    }

    fun startActivityForClear(clazz: Class<*>) {
        startActivity(Intent.makeRestartActivityTask(context?.let { ComponentName(it, clazz) }))
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}