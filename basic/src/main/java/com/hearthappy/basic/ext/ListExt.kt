package com.hearthappy.basic.ext

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
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

/**
 * 数组/集合安全取值工具类
 * 所有扩展函数逻辑：index 有效则返回对应值，越界返回自定义默认值
 */

// ===================== 原始类型数组（Primitive Array） =====================
/** IntArray 安全取值 */
fun IntArray.safeGet(index: Int, default: Int = -1): Int {
    return if (index in indices) this[index] else default
}

/** LongArray 安全取值 */
fun LongArray.safeGet(index: Int, default: Long = -1): Long {
    return if (index in indices) this[index] else default
}

/** FloatArray 安全取值 */
fun FloatArray.safeGet(index: Int, default: Float = -1f): Float {
    return if (index in indices) this[index] else default
}

/** DoubleArray 安全取值 */
fun DoubleArray.safeGet(index: Int, default: Double = -1.0): Double {
    return if (index in indices) this[index] else default
}

/** CharArray 安全取值 */
fun CharArray.safeGet(index: Int, default: Char = ' '): Char {
    return if (index in indices) this[index] else default
}

/** BooleanArray 安全取值 */
fun BooleanArray.safeGet(index: Int, default: Boolean = false): Boolean {
    return if (index in indices) this[index] else default
}

// ===================== 泛型数组（Generic Array） =====================
/** 任意类型数组（如 Array<String>、Array<Int>）安全取值 */
fun <T> Array<T>.safeGet(index: Int, default: T): T {
    return if (index in indices) this[index] else default
}

// ===================== List 集合 =====================
/** List 集合安全取值（适配所有 List 子类：ArrayList、MutableList 等） */
fun <T> List<T>.safeGet(index: Int, default: T): T { // List 没有 indices 属性，用 0 until size 判断有效索引
    return if (index in 0 until size) this[index] else default
}