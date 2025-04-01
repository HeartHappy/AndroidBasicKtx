package com.hearthappy.base.ext

import android.app.Activity
import android.os.Build
import android.transition.Fade
import android.transition.Transition
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding


var pwMap: MutableMap<String, PopupWindow>? = null

/**
 * @Author ChenRui
 * @Email 1096885636@qq.com
 * @Date 11/1/24
 * @Describe 弹窗工具类
 * 优点：
 *      1、解决了原生繁琐的创建文件类问题。
 *      2、解决了原生在不同PopupWindow视图情况下，需要分别创建文件类，一系列视图绑定和接口回调
 *      3、一行代码通过扩展函数传相应参数创建属于自己的视图
 *      4、解决了原生绑定View事件时需要通过接口回调得到相应的value
 *
 * 功能：
 *      1、支持单例模式构建
 *      2、支持多实例构建。
 *      3、支持自定义ViewBinding
 *      4、支持自定义视图宽高
 *      5、支持PopupWindow域中操作ViewBinding事件绑定
 *      6、支持多popupWindow同时显示（多实例创建）。如果没有被用户dismiss的，不被释放，下次使用直接使用缓存，如果需要新建window和上次创建且未销毁的并存，则通过key区分.已完成
 *      7、支持自定义屏幕外点击是否dismiss：默认值：true（点击屏幕外消失）
 *      8、支持弹窗后，自定义window背景模糊，默认值：0.4f
 *      9、支持自定义弹窗动画。默认值：Fade动画
 *      10、支持自定义弹窗位置。分别支持：1、相对某个View位置，2、相对整个屏幕位置
 *      11、支持使用场景：Activity、Fragment
 */
fun <VB : ViewBinding> AppCompatActivity.popupWindow(viewBinding: VB, viewEventListener: PopupWindow.(VB) -> Unit, width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT, focusable: Boolean = true, isOutsideTouchable: Boolean = true, //点击布局外是否消失，true：消失
                                                     backgroundBlackAlpha: Float = 0.4f, anim: Transition = Fade(), windowType: Int = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL, key: String = "default"): PopupWindow {
    return handlerPopupWindow(key, viewBinding, width, height, viewEventListener, focusable, isOutsideTouchable, backgroundBlackAlpha, anim, windowType)
}

fun <VB : ViewBinding> Fragment.popupWindow(
    viewBinding: VB, viewEventListener: PopupWindow.(VB) -> Unit, width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT, focusable: Boolean = true, isOutsideTouchable: Boolean = true, //点击布局外是否消失，true：消失
    backgroundBlackAlpha: Float = 0.4f, anim: Transition = Fade(), windowType: Int = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL, key: String = "default",
): PopupWindow {
    return (requireActivity() as AppCompatActivity).handlerPopupWindow(key, viewBinding, width, height, viewEventListener, focusable, isOutsideTouchable, backgroundBlackAlpha, anim, windowType)
}

fun <VB : ViewBinding> AppCompatActivity.handlerPopupWindow(key: String, viewBinding: VB, width: Int, height: Int, viewEventListener: PopupWindow.(VB) -> Unit, focusable: Boolean, isOutsideTouchable: Boolean, //点击布局外是否消失，true：消失
                                                            backgroundBlackAlpha: Float, anim: Transition = Fade(), windowType: Int): PopupWindow {

    if (pwMap == null) {
        pwMap = mutableMapOf()
        lifecycle.addObserver(ActivityDestroyObserver())
    }
    return pwMap?.get(key) ?: object : PopupWindow(viewBinding.root, width, height, focusable) {
        override fun dismiss() {
            super.dismiss()
            pwMap?.remove(key)
            setBackgroundBlackAlpha(1f)
        }
    }.run {
        viewEventListener(this, viewBinding) //初始化PopupWindow属性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //点击屏幕外是否消失
            this.isTouchModal = isOutsideTouchable
        }
        this.isOutsideTouchable = isOutsideTouchable
        this.isFocusable = focusable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.enterTransition = anim
            this.exitTransition = anim
            windowLayoutType = windowType
        }
        this.setBackgroundDrawable(null) //背景黑
        setBackgroundBlackAlpha(backgroundBlackAlpha)
        pwMap?.put(key, this)
        this
    }
}


fun PopupWindow.show(showAtLocation: (() -> ShowAtLocation)? = null, showAsDropDown: (() -> ShowAsDropDown)? = null) {
    if (!isShowing) {
        showAtLocation?.apply {
            val location = this()
            showAtLocation(location.relativeView, location.gravity, location.x, location.y)
        }
        showAsDropDown?.apply {
            val dropDown = this()
            showAsDropDown(dropDown.anchor, dropDown.x, dropDown.y, dropDown.gravity)
        }
    }
}

fun PopupWindow.showLocation(relativeView: View, gravity: Int = Gravity.CENTER, x: Int = 0, y: Int = 0) {
    showAtLocation(relativeView, gravity, x, y)
}

fun PopupWindow.showDropDown(anchor: View, gravity: Int = Gravity.TOP or Gravity.START, x: Int = 0, y: Int = 0) {
    showAsDropDown(anchor, x, y, gravity)
}

fun Activity.setBackgroundBlackAlpha(backgroundBlackAlpha: Float) {
    val lp: WindowManager.LayoutParams = window.attributes //    lp.dimAmount=dim//0:完全透明，1:完全不透明(黑色背景)
    lp.alpha = backgroundBlackAlpha
    window.attributes = lp
}


data class ShowAtLocation(val relativeView: View, val gravity: Int = Gravity.CENTER, val x: Int = 0, val y: Int = 0)

data class ShowAsDropDown(val anchor: View, val gravity: Int = Gravity.TOP or Gravity.START, val x: Int = 0, val y: Int = 0)

class ActivityDestroyObserver : DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) { // 当 Activity 销毁时，将 pwMap 置为 null
        pwMap = null
    }
}