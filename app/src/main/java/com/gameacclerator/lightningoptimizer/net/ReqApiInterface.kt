package com.gameacclerator.lightningoptimizer.net

import com.gameacclerator.lightningoptimizer.bean.IpBean
import retrofit2.Response

/*请求标准接口*/
interface ReqApiInterface {
    fun detectIp(reqCallBack: ReqCallBack<Response<IpBean>>)
    fun getVpnServiceList()
    fun getVpnSmartList()
    fun getAdDataList()
}