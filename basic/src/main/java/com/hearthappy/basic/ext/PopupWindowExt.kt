package com.hearthappy.basic.ext

import android.app.Activity
import android.os.Build
import android.transition.Fade
import android.transition.Transition
import android.util.Log
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
import java.lang.ref.WeakReference
import java.util.WeakHashMap

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
 *      12、安全的自动回收机制
 */
object PopupManager {
    internal val popupMap = WeakHashMap<WeakReference<LifecycleOwner>, MutableMap<String, PopupWindow>>()
    internal const val TAG = "PopupManager"

    var isAutoRecycling = true

    /**
     * 注册弹窗
     */
    fun registerPopup(owner: LifecycleOwner, key: String, popup: PopupWindow) { // 查找或创建Owner的弱引用
        val ownerRef = findOwnerRef(owner) ?: WeakReference(owner).also { // 首次注册时绑定生命周期
            bindLifecycle(owner, it)
        }

        // 获取或创建该Owner的弹窗集合
        val ownerPopups = popupMap.getOrPut(ownerRef) { mutableMapOf() }
        ownerPopups[key] = popup
    }

    /**
     * 取消注册弹窗
     */
    fun unregisterPopup(owner: LifecycleOwner, key: String) {
        findOwnerRef(owner)?.let { ownerRef ->
            popupMap[ownerRef]?.remove(key)

            // 如果该owner的所有弹窗都已移除，移除整个entry
            if (popupMap[ownerRef]?.isEmpty() == true) {
                popupMap.remove(ownerRef)
            }
        }
    }

    /**
     * 安全关闭弹窗
     */
    fun dismissSafely(popup: PopupWindow) {
        try {
            if (popup.isShowing && isAutoRecycling) popup.dismiss()
        } catch (e: Exception) {
            Log.e(TAG, "Safe dismiss failed: ${e.message}")
        }
    }

    /**
     * 清除指定Owner的所有弹窗
     */
    private fun clearPopupsForOwner(ownerRef: WeakReference<LifecycleOwner>) { // 创建副本避免ConcurrentModificationException
        val popups = popupMap[ownerRef]?.values?.toList() ?: return

        popups.forEach { popup ->
            if (popup.isShowing) {
                dismissSafely(popup)
            }
        }

        // 移除整个owner条目
        popupMap.remove(ownerRef)
    }

    /**
     * 查找对应的Owner引用
     */
    internal fun findOwnerRef(owner: LifecycleOwner): WeakReference<LifecycleOwner>? {
        return popupMap.keys.firstOrNull { it.get() == owner }
    }

    /**
     * 绑定生命周期监听
     */
    private fun bindLifecycle(owner: LifecycleOwner, ownerRef: WeakReference<LifecycleOwner>) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) { // 在onPause时关闭弹窗（更早的时机）
                val popups = popupMap[ownerRef]?.values?.toList() ?: return

                popups.forEach { popup ->
                    if (popup.isShowing && isAutoRecycling) {
                        Log.d(TAG, "Closing popup in onPause for ${owner.javaClass.simpleName}")
                        dismissSafely(popup)
                    }
                }
            }


            override fun onDestroy(owner: LifecycleOwner) { // 清理所有弹窗
                clearPopupsForOwner(ownerRef)

                // 移除监听器
                owner.lifecycle.removeObserver(this)
            }
        })
    }
}

/**
 * Activity扩展函数
 */
fun <VB : ViewBinding> AppCompatActivity.popupWindow(viewBinding: VB, viewEventListener: PopupWindow.(VB) -> Unit, width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT, focusable: Boolean = true, isOutsideTouchable: Boolean = true, backgroundBlackAlpha: Float = 0.4f, anim: Transition = Fade(), windowType: Int = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL, key: String = "default"): PopupWindow {
    return handlerPopupWindow(key, viewBinding, width, height, viewEventListener, focusable, isOutsideTouchable, backgroundBlackAlpha, anim, windowType)
}

/**
 * Fragment扩展函数
 */
fun <VB : ViewBinding> Fragment.popupWindow(viewBinding: VB, viewEventListener: PopupWindow.(VB) -> Unit, width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT, focusable: Boolean = true, isOutsideTouchable: Boolean = true, backgroundBlackAlpha: Float = 0.4f, anim: Transition = Fade(), windowType: Int = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL, key: String = "default"): PopupWindow {
    return (requireActivity() as AppCompatActivity).handlerPopupWindow(key, viewBinding, width, height, viewEventListener, focusable, isOutsideTouchable, backgroundBlackAlpha, anim, windowType)
}

/**
 * 处理弹窗创建和复用
 */
private fun <VB : ViewBinding> AppCompatActivity.handlerPopupWindow(key: String, viewBinding: VB, width: Int, height: Int, viewEventListener: PopupWindow.(VB) -> Unit, focusable: Boolean, isOutsideTouchable: Boolean, backgroundBlackAlpha: Float, anim: Transition, windowType: Int): PopupWindow { // 尝试获取已存在的弹窗
    val existingPopup = PopupManager.findOwnerRef(this)?.let { ownerRef ->
        PopupManager.popupMap[ownerRef]?.get(key)
    }

    // 如果存在且正在显示，则直接返回
    if (existingPopup != null && existingPopup.isShowing) {
        return existingPopup
    }

    return object : PopupWindow(viewBinding.root, width, height, focusable) {
        override fun dismiss() {
            try {
                super.dismiss()
            } finally { // 取消注册
                PopupManager.unregisterPopup(this@handlerPopupWindow, key)

                // 检查当前Activity是否还有弹窗显示
                if (shouldRestoreBackground()) {
                    setBackgroundBlackAlpha(1f) // 取消注册并恢复背景透明度
                }
            }
        }
    }.apply { // 初始化弹窗属性
        viewEventListener(this, viewBinding)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.isTouchModal = isOutsideTouchable
        }

        this.isOutsideTouchable = isOutsideTouchable
        this.isFocusable = focusable

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.enterTransition = anim
            this.exitTransition = anim
            windowLayoutType = windowType
        }

        this.setBackgroundDrawable(null) // 设置背景透明度（仅在第一个弹窗显示时设置）
        if (shouldSetBackground()) {
            setBackgroundBlackAlpha(backgroundBlackAlpha)
        }

        // 注册到管理器
        PopupManager.registerPopup(this@handlerPopupWindow, key, this)
    }
}

/**
 * 检查是否应该设置背景透明度
 */
private fun AppCompatActivity.shouldSetBackground(): Boolean { // 检查当前Activity是否已有其他弹窗显示
    val ownerRef = PopupManager.findOwnerRef(this) ?: return true
    return PopupManager.popupMap[ownerRef]?.isEmpty() ?: true
}

/**
 * 检查是否应该恢复背景透明度
 */
private fun AppCompatActivity.shouldRestoreBackground(): Boolean { // 检查当前Activity是否还有其他弹窗显示
    val ownerRef = PopupManager.findOwnerRef(this) ?: return true
    return PopupManager.popupMap[ownerRef]?.isEmpty() ?: true
}

/**
 * 显示弹窗（位置相关）
 */
fun PopupWindow.show(showAtLocation: (() -> ShowAtLocation)? = null, showAsDropDown: (() -> ShowAsDropDown)? = null) {
    if (!isShowing) {
        showAtLocation?.invoke()?.let {
            showAtLocation(it.relativeView, it.gravity, it.x, it.y)
        }

        showAsDropDown?.invoke()?.let {
            showAsDropDown(it.anchor, it.x, it.y, it.gravity)
        }
    }
}
fun PopupWindow.showAtCenter(relativeView: View, x: Int = 0, y: Int = 0) {
    showAtLocation(relativeView, Gravity.CENTER, x, y)
}
fun PopupWindow.showAtTop(relativeView: View, x: Int = 0, y: Int = 0) {
    showAtLocation(relativeView, Gravity.TOP, x, y)
}
fun PopupWindow.showAtBottom(relativeView: View, x: Int = 0, y: Int = 0) {
    showAtLocation(relativeView, Gravity.BOTTOM, x, y)
}
fun PopupWindow.showAtLeft(relativeView: View, x: Int = 0, y: Int = 0) {
    showAtLocation(relativeView, Gravity.LEFT, x, y)
}
fun PopupWindow.showAtRight(relativeView: View, x: Int = 0, y: Int = 0) {
    showAtLocation(relativeView, Gravity.RIGHT, x, y)
}




fun PopupWindow.showDropDown(anchor: View, gravity: Int = Gravity.TOP or Gravity.START, x: Int = 0, y: Int = 0) {
    showAsDropDown(anchor, x, y, gravity)
}

/**
 * 设置背景透明度
 */
fun Activity.setBackgroundBlackAlpha(backgroundBlackAlpha: Float) {
    if (isFinishing || isDestroyed) return

    runOnUiThread {
        try {
            val lp = window.attributes
            lp.alpha = backgroundBlackAlpha
            window.attributes = lp
        } catch (e: Exception) {
            Log.e(PopupManager.TAG, "setBackgroundBlackAlpha failed: ${e.message}")
        }
    }
}

/**
 * 位置数据类
 */
data class ShowAtLocation(val relativeView: View, val gravity: Int = Gravity.CENTER, val x: Int = 0, val y: Int = 0)
data class ShowAsDropDown(val anchor: View, val gravity: Int = Gravity.TOP or Gravity.START, val x: Int = 0, val y: Int = 0)