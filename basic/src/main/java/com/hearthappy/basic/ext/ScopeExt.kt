package com.hearthappy.basic.ext

inline fun  multipleLet(vararg args: Any?, block: (List<Any>) -> Unit){
    if (args.any { it == null }) {
        println("multipleLet: args contain null value")
        return
    }
    block(args.filterNotNull())
}