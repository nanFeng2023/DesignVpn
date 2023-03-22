package com.nfgz.zgg.net.retrofit

import com.nfgz.zgg.bean.IpBean
import com.nfgz.zgg.util.ConstantUtil
import retrofit2.Call
import retrofit2.http.GET

interface ApiRetrofit {

    @GET(ConstantUtil.IP_PATH)
    fun getIpBeanCall(): Call<IpBean>


}