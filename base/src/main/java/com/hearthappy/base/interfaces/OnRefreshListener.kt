package com.hearthappy.base.interfaces

import androidx.viewbinding.ViewBinding

interface OnRefreshListener {

    fun onRefreshProgress(viewBinding: ViewBinding,progress:Float)

    fun onRefreshFinish()

}