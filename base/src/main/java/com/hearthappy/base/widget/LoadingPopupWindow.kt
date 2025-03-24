package com.hearthappy.base.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.hearthappy.androidbasicktx.databinding.DialogLoadingBinding

/**
 * Created Date 2021/1/21.
 * @author ChenRui
 * ClassDescription:
 */
class LoadingPopupWindow(val context: Context) : PopupWindow(context) {

    private var viewBinding: DialogLoadingBinding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
    private var animationDrawable: AnimationDrawable


    init {
        contentView = viewBinding.root
        viewBinding.apply {
            animationDrawable = ivLoading.background as AnimationDrawable
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT //取消背景黑色透明
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //点击屏幕是否消失
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isTouchModal = false
            }
            isOutsideTouchable = true //是否支持焦点
            isFocusable = true
            setOnDismissListener { animationDrawable.stop() }
        }
    }


    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        animationDrawable.start()
        super.showAtLocation(parent, gravity, x, y)
    }
}