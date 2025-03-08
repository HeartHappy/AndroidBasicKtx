package com.hearthappy.androidbasiclibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _ldData = MutableLiveData<List<String>>()
    val ld: LiveData<List<String>> = _ldData

    fun getListData() {
        val listOf = arrayListOf<String>()
        for (i in 0..100) {
            listOf.add(i.toString())
        }
        _ldData.value = listOf
    }
}