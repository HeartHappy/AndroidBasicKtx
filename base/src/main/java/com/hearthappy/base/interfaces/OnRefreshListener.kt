package com.hearthappy.base.interfaces

import androidx.viewbinding.ViewBinding

interface OnRefreshListener<VB : ViewBinding> {

    fun onRefreshProgress(viewBinding: VB,progress:Float)

    fun onRefreshFinish(viewBinding: VB)

}