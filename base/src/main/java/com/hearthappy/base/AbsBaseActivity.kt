package com.hearthappy.base

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.hearthappy.androidbasicktx.R
import com.hearthappy.androidbasicktx.databinding.ToolbarBackLayoutBinding
import com.hearthappy.base.ext.findActivityInflate
import com.hearthappy.base.widget.LoadingPopupWindow

/**
 * Created Date: 2024/11/25
 * @author ChenRui
 * ClassDescription： Activity基类
 */
abstract class AbsBaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var viewBinding: VB
    private var alertDialog: AlertDialog? = null
    private lateinit var loadingDialog: LoadingPopupWindow

    /**
     * 获取ViewModel
     */
    fun <VM : ViewModel> getViewModel(c: Class<VM>, factory: ViewModelProvider.Factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)): VM { //        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        return ViewModelProvider(this, factory).get(c)
    }

    /** 等待对话框 */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        viewBinding = initViewBinding() ?: findActivityInflate()
        setContentView(viewBinding.root)
        viewBinding.apply {
            initView()
            initViewModelListener()
            initListener()
            initData()
        }
        window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
        if (openGrayscaleSwitch()) openGrayscale()
    }

    open fun openGrayscaleSwitch(): Boolean = false


    /**
     * 开启灰度页面
     */
    private fun openGrayscale() {
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0F) //灰度效果
        paint.colorFilter = ColorMatrixColorFilter(cm)
        window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
    }

    fun showDialogHint(title: String, text: String, themeId: Int = android.R.style.Theme_Material_Dialog, confirm: () -> Unit, cancel: () -> Unit) {
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
        }
        builder.setTitle(title)
        builder.setMessage(text)
        alertDialog = builder.create()
        alertDialog?.show()
    }

    private fun dismissDialog() {
        alertDialog?.apply {
            if (this.isShowing) this.dismiss()
            alertDialog = null
        }
    }

    open fun showLoadingDialog(parentView: View, loadingPopupWindow: LoadingPopupWindow = LoadingPopupWindow(this)) {
        loadingDialog = loadingPopupWindow
        loadingDialog.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

    open fun dismissLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }


    open fun initViewBinding(): VB? = null

    abstract fun VB.initView()
    abstract fun VB.initViewModelListener()
    abstract fun VB.initListener()
    abstract fun VB.initData()


    fun startActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun startActivityOptions(clazz: Class<*>) {
        startActivity(Intent(this, clazz::class.java), ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun startActivityOptions(intent: Intent) {
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun startActivityForClear(clazz: Class<*>) {
        startActivity(Intent.makeRestartActivityTask(ComponentName(this, clazz)))
    }

    fun initNavController() { //获取mFragments成员变量
        val mFragmentsField = FragmentActivity::class.java.getDeclaredField("mFragments").apply {
            isAccessible = true
        } //获取mCreated成员变量
        val mCreatedField = FragmentActivity::class.java.getDeclaredField("mCreated").apply {
            isAccessible = true
        } //获取dispatchActivityCreated方法
        val dispatchActivityCreatedMethod = FragmentController::class.java.getDeclaredMethod("dispatchActivityCreated").apply {
            isAccessible = true
        } //调用dispatchActivityCreated方法
        dispatchActivityCreatedMethod.invoke(mFragmentsField.get(this))

        //别忘了把mCreated设置为true，防止dispatchActivityCreated在onStart中再次调用
        mCreatedField.set(this, true)
    }

    fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) view = View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun ToolbarBackLayoutBinding.init(isBlack: Boolean = true,title: String?, @DrawableRes rightIcon: Int? = null, leftIconListener: (() -> Unit)? = null, rightIconListener: () -> Unit = {}) {
        this.ivlIcon.setOnClickListener { leftIconListener?.let { it() } ?: finishAfterTransition() }
        this.tvTitle.text = title
        if (isBlack) {
            val colorBlack = ContextCompat.getColor(this@AbsBaseActivity, R.color.color_base_black)
            ivlIcon.setColorFilter(colorBlack)
            ivrIcon.setColorFilter(colorBlack)
            tvTitle.setTextColor(colorBlack)
        } else {
            val colorWhite = ContextCompat.getColor(this@AbsBaseActivity, R.color.color_base_white)
            tvTitle.setTextColor(colorWhite)
            ivlIcon.setColorFilter(colorWhite)
            ivrIcon.setColorFilter(colorWhite)
        }
        rightIcon?.let {
            this.ivrIcon.setImageResource(it)
            this.ivrIcon.visibility = View.VISIBLE
            this.ivrIcon.setOnClickListener { rightIconListener() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialog()
        dismissLoadingDialog()
    }
}