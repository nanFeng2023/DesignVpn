package com.nfgz.zgg.net

import com.nfgz.zgg.bean.IpBean
import com.nfgz.zgg.net.retrofit.RetrofitUtil
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

    }
}