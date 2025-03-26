package com.hearthappy.base.tools.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*


/**
 * Created Date 2020/12/18.
 *
 * @author ChenRui
 * ClassDescription:
 */
internal object LocationTools {


    /**
     * 获取地理位置信息
     */
    fun getLocationAddress(context: Context): LocationBean? { //获取定位管理对象
        val lm =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager //        List<String> names = lm.getAllProviders();//获取所有的位置提供者，一般三种
        val criteria = Criteria() //查询条件，如果设置了海拔，则定位方式只能是GPS;
        criteria.isCostAllowed = true //是否产生开销，比如流量费
        val provider = lm.getBestProvider(criteria, true) //获取最好的位置提供者，第二个参数为true，表示只获取那些被打开的位置提供者
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        if (provider != null) { //获取位置。第二个参数表示每隔多少时间返回一次数据，第三个参数表示被定位的物体移动每次多少米返回一次数据。
            val location = lm.getLastKnownLocation(provider)
            if (location != null) { //                String locationToString = locationToString(location);
                return getAddressByLocation(context, location)
            }
        }
        return null
    }


    /**
     * 自动更新，无需反复调用
     */
   inline fun getLocationAddress2(context: Context, crossinline block:(locationBean: LocationBean)->Unit) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val criteria = Criteria()
        criteria.accuracy=Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired=true
        criteria.isBearingRequired=true
        criteria.isSpeedRequired=true
        criteria.bearingAccuracy=Criteria.ACCURACY_LOW
        criteria.speedAccuracy=Criteria.ACCURACY_HIGH
        criteria.powerRequirement=Criteria.POWER_LOW
        val bestProvider = lm.getBestProvider(criteria, true)
        bestProvider?.let {
            lm.requestLocationUpdates(it, 0, 0f) { location ->
                val locationAddress = getAddressByLocation(context, location)
                locationAddress?.let { it1 -> block(it1) }
            }
        }

    }


    /**
     * 将经纬度转换成中文地址
     *
     * @param location
     * @return
     */
    internal fun getAddressByLocation(
        context: Context, location: Location
    ): LocationBean? {
        val geoCoder = Geocoder(context, Locale.CHINESE)
        try {
            val addresses = geoCoder.getFromLocation(
                location.latitude, location.longitude, 1
            )
            val address = addresses[0]
            val addressDetail = address.getAddressLine(0) //详细地址
            val countryName = address.countryName //国家
            val countryCode = address.countryCode //国家代码
            val adminArea = address.adminArea //省
            val locality = address.locality //市区
            val featureName = address.featureName //地址
            val longitude = address.longitude //经度
            val latitude = address.latitude //纬度
            return LocationBean(
                addressDetail,
                countryName,
                countryCode,
                adminArea,
                locality,
                featureName,
                longitude,
                latitude
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}