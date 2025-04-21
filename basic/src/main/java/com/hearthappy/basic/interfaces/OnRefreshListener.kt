package com.hearthappy.basic.interfaces

import androidx.viewbinding.ViewBinding

interface OnRefreshListener {

    fun onRefreshProgress(progress:Float)

    fun onRefreshFinish(progress:Float)

}