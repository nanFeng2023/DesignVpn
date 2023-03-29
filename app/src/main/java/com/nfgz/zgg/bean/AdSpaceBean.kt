package com.nfgz.zgg.bean

import com.nfgz.zgg.inter.AdReqResultCallBack
import com.nfgz.zgg.util.ConstantUtil
import java.util.*
import kotlin.collections.ArrayList

/*广告位对象*/
class AdSpaceBean(spaceType: String) {
    //过期时间
    var expirationTime: Long = 0L

    //能否重新加载，默认可以重试
    var canReload = true

    //是否正在请求
    var requesting = false

    //广告位类型
    var spaceType: String? = spaceType

    //子广告类型
    var itemAdType: String? = null

    //广告对象
    var ad: Any? = null

    //广告集合对象
    var adBeanList: ArrayList<AdBean> = ArrayList()

    var callBack: AdReqResultCallBack? = null

    fun isAdNotExpiration(): Boolean {//判断广告是否过期
        val dateDifference: Long = Date().time - expirationTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * ConstantUtil.AD_EXPIRATION_TIME
    }

    fun isAdAvailable(): Boolean {
        return ad != null && isAdNotExpiration()
    }

    fun allReqAdFinished(): Boolean {
        if (!adBeanList.iterator().hasNext() && !requesting) {
            return true
        }
        return false
    }

}