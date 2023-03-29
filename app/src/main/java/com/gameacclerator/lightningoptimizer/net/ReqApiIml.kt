package com.gameacclerator.lightningoptimizer.net

import com.gameacclerator.lightningoptimizer.bean.IpBean
import com.gameacclerator.lightningoptimizer.net.retrofit.RetrofitUtil
import retrofit2.Response

class ReqApiIml : ReqApiInterface {
    override fun detectIp(reqCallBack: ReqCallBack<Response<IpBean>>) {
        RetrofitUtil.detectIp(reqCallBack)
    }


    override fun getVpnServiceList() {
        RetrofitUtil.getServiceListData()
    }

    override fun getVpnSmartList() {
        RetrofitUtil.getSmartServiceListData()
    }

    override fun getAdDataList() {
        RetrofitUtil.getAdListData()
    }
}