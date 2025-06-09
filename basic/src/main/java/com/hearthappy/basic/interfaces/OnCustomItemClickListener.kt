package com.hearthappy.basic.interfaces

import android.view.View

interface OnCustomItemClickListener<T> {

    fun onCustomItemClick(view: View, data: T, position: Int, listPosition: Int)
}