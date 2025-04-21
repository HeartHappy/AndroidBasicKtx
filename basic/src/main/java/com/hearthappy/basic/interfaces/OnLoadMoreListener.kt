package com.hearthappy.basic.interfaces

import androidx.viewbinding.ViewBinding

interface OnLoadMoreListener {
    fun onLoadMoreProgress(progress:Float)

    fun onLoadMoreFinish(progress:Float)
}