package com.hearthappy.basic.model

import com.hearthappy.basic.interfaces.ICustomItemSupper

data class CustomItemView(val viewType  : Int, val supper : ICustomItemSupper<*,*>)