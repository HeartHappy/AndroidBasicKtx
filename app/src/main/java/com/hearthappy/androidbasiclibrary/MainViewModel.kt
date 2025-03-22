package com.hearthappy.androidbasiclibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _ldData = MutableLiveData<List<String>>()
    val ld: LiveData<List<String>> = _ldData

    fun getListData() {
        val listOf = arrayListOf<String>()
        repeat(10) { listOf.add("is data:") }
        _ldData.value = listOf
    }
}