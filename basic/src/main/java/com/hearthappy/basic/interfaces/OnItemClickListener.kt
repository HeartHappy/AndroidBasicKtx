package com.hearthappy.basic.interfaces

import android.view.View

interface OnItemClickListener<T> {
    fun onItemClick(view: View, data: T, position: Int, listPosition: Int)
}