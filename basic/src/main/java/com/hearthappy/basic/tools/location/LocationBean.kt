package com.hearthappy.basic.tools.location

/**
 * Created Date 2020/12/18.
 * @author ChenRui
 * ClassDescription:
 */
data class LocationBean(//详细地址
        var addressDetail: String?,//国家
        var countryName: String?,//国家代码
        var countryCode: String?,//国家代码
        var adminArea: String?,//省
        var locality: String?,//市区
        var featureName: String?,//地址
        var longitude: Double,//经度
        var latitude: Double//纬度
)