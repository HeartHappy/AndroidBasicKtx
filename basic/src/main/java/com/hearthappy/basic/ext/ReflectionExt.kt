package com.hearthappy.basic.ext

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
fun Class<*>.findInterfaceInflate(container: ViewGroup?, interfaceClazz: Class<*>): ViewBinding {
    val type = this.genericInterfaces.find { it is ParameterizedType && it.rawType == interfaceClazz }
    if (type is ParameterizedType) {
        val clazz = type.actualTypeArguments[0] as Class<ViewBinding>
        try {
            val inflateMethod = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            return inflateMethod.invoke(null, LayoutInflater.from(container?.context), container, false) as ViewBinding
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
    throw IllegalArgumentException("Failed to get ViewBinding instance.")
}

@Suppress("UNCHECKED_CAST") fun <VB : ViewBinding> Activity.findActivityInflate(): VB {
    val type = javaClass.genericSuperclass
    if (type is ParameterizedType) {
        val clazz = type.actualTypeArguments[0] as Class<VB>
        try { // 获取所有方法并打印
            val allMethods = clazz.declaredMethods
            val inflateMethod = allMethods.find { it.name == "inflate" && it.parameterTypes.size == 1 && it.parameterTypes[0] == android.view.LayoutInflater::class.java } // 尝试查找 inflate 方法
            return inflateMethod?.invoke(null, layoutInflater) as VB
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
    throw IllegalArgumentException("Failed to get ViewBinding instance.")
}


@Suppress("UNCHECKED_CAST")
fun <VB : ViewBinding> Fragment.findFragmentInflate(inflater: LayoutInflater, container: ViewGroup?): VB {
    val type = javaClass.genericSuperclass
    if (type is ParameterizedType) {
        val clazz = type.actualTypeArguments[0] as Class<VB>
        try {
            val inflateMethod = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            return inflateMethod.invoke(null, inflater, container, false) as VB
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
    throw IllegalArgumentException("Failed to get ViewBinding instance.")
}

@Suppress("UNCHECKED_CAST")
 fun <VB:ViewBinding> RecyclerView.Adapter<RecyclerView.ViewHolder>.findAdapterInflate(inflater: LayoutInflater, container: ViewGroup?): VB {
    val type = javaClass.genericSuperclass
    if (type is ParameterizedType) {
        val clazz = type.actualTypeArguments[0] as Class<VB>
        try {
            val inflateMethod = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            return inflateMethod.invoke(null, inflater, container, false) as VB
        } catch (e: IllegalAccessException) {
            Log.e("AbsBaseAdapter", "IllegalAccessException: ${e.message}", e)
        } catch (e: InvocationTargetException) {
            Log.e("AbsBaseAdapter", "InvocationTargetException: ${e.message}", e)
        }
    }
    throw IllegalArgumentException("Failed to get ViewBinding instance.")
}