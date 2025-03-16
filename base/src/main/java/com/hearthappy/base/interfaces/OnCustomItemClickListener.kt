package com.hearthappy.base.interfaces

import android.view.View

interface OnCustomItemClickListener {

    fun onInsetItemClick(view: View, position: Int, customPosition: Int)
}