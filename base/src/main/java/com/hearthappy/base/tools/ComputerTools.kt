package com.hearthappy.base.tools

import kotlin.math.ceil

object ComputerTools {


    /**
     * 计算分页
     * @param pageSize Int 每页加载数量
     * @param count Int 总数量
     * @return Int 总页数
     */
    fun pagination(pageSize:Int,count:Int):Int{
        return ceil((count.toDouble()/pageSize)).toInt()
    }

    /**
     * 点赞数量转换
     * @param likesCount Int
     * @return String
     */
    fun formatLikesCount(likesCount: Int): String {
        return when {
            likesCount < 1000 -> likesCount.toString()
            likesCount < 10000 -> String.format("%.1fK", likesCount / 1000.0)
            else -> String.format("%.1f万", likesCount / 10000.0)
        }
    }

    /**
     * 计算图片宽高。高于1920则除以2
     * @param width Int
     * @param height Int
     * @return Pair<Int, Int>
     */
    fun calculateImageSize(width: Int, height: Int): Pair<Int, Int> {
        var newWidth = width
        var newHeight = height

        while (newHeight >= 1920 || newWidth>=1920) {
            newWidth /= 2
            newHeight /= 2
        }

        return Pair(newWidth, newHeight)
    }
}