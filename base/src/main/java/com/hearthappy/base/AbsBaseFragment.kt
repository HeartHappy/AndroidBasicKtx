package com.hearthappy.base

import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hearthappy.androidbasicktx.R
import com.hearthappy.base.ext.findFragmentInflate
import com.hearthappy.base.widget.LoadingPopupWindow


/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 10/11/24
 * @Describe 基础fragment
 */
abstract class AbsBaseFragment<VB : ViewBinding> : Fragment() {


    private var _binding: VB? = null
    protected val viewBinding get() = _binding!!
    private var alertDialog: AlertDialog? = null
    private var loadingDialog: PopupWindow? = null

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

    fun showDialogHint(title: String, text: String, themeId: Int = android.R.style.Theme_Material_Dialog, confirm: () -> Unit, cancel: () -> Unit) {
        context?.run {
            val builder = AlertDialog.Builder(this, themeId)
            builder.apply {
                setPositiveButton(R.string.ok) { dialog, id ->
                    dismissDialog()
                    confirm()
                }
                setNegativeButton(R.string.cancel) { dialog, id ->
                    dismissDialog()
                    cancel()
                }
                builder.setTitle(title)
                builder.setMessage(text)
                alertDialog = builder.create()
                alertDialog?.show()
            }
        }
    }

    private fun dismissDialog() {
        alertDialog?.apply {
            if (isShowing) dismiss()
            alertDialog = null
        }
    }

    open fun showLoadingDialog(parentView: View, loadingPopupWindow: PopupWindow? = context?.let { LoadingPopupWindow(it) }) {
        loadingDialog = loadingPopupWindow
        loadingDialog?.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

    open fun dismissLoadingDialog() {
        loadingDialog?.apply {
            if (isShowing)dismiss()
            loadingDialog = null
        }
    }

    fun startActivity(clazz: Class<*>) {
        startActivity(Intent(context, clazz))
    }

    fun startActivityOptions(clazz: Class<*>) {
        startActivity(Intent(context, clazz::class.java), ActivityOptions.makeSceneTransitionAnimation(context as Activity?).toBundle())
    }

    fun startActivityOptions(intent: Intent) {
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context as Activity?).toBundle())
    }

    fun startActivityForClear(clazz: Class<*>) {
        startActivity(Intent.makeRestartActivityTask(context?.let { ComponentName(it, clazz) }))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        dismissDialog()
        dismissLoadingDialog()
    }

}