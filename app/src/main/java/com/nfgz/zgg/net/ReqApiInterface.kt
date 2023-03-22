package com.nfgz.zgg.net

import com.nfgz.zgg.bean.IpBean
import retrofit2.Response

/*请求标准接口*/
interface ReqApiInterface {
    fun detectIp(reqCallBack: ReqCallBack<Response<IpBean>>)
    fun getVpnServiceList()
    fun getVpnSmartList()
    fun getAdDataList()
}