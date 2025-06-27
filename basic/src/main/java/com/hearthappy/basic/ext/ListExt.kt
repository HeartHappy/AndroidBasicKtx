package com.hearthappy.basic.ext

@Suppress("UNCHECKED_CAST") fun <T> List<T>.toArrayList(): ArrayList<T> {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") val thisCollection = this as java.util.Collection<T>
    val arrayList = ArrayList<T>()
    for (t in thisCollection) {
        arrayList.add(t)
    }
    return arrayList
}

/**
 * 划分集合根据索引为条件
 * @receiver Iterable<T>
 * @param predicate Function2<Int, T, Boolean>
 * @return Pair<List<T>, List<T>>
 */
inline fun <T> Iterable<T>.partitionByIndex(predicate: (Int) -> Boolean): Pair<List<T>, List<T>> {
    val first = ArrayList<T>()
    val second = ArrayList<T>()
    forEachIndexed { index, element ->
        if (predicate(index)) {
            first.add(element)
        } else {
            second.add(element)
        }
    }
    return Pair(first, second)
}