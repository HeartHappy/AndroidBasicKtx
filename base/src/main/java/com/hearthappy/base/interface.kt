package com.hearthappy.base

import android.view.View

interface OnItemClickListener<T> {

    fun onItemClick(view: View, data: T, position: Int)

}