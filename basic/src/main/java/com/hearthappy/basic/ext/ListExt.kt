package com.hearthappy.basic.ext

@Suppress("UNCHECKED_CAST") fun <T> List<T>.toArrayList(): ArrayList<T> {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") val thisCollection = this as java.util.Collection<T>
    val arrayList = ArrayList<T>()
    for (t in thisCollection) {
        arrayList.add(t)
    }
    return arrayList
}