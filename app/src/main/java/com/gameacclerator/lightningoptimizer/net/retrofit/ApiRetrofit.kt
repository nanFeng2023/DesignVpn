package com.gameacclerator.lightningoptimizer.net.retrofit

import com.gameacclerator.lightningoptimizer.bean.IpBean
import com.gameacclerator.lightningoptimizer.util.ConstantUtil
import retrofit2.Call
import retrofit2.http.GET

interface ApiRetrofit {

    @GET(ConstantUtil.IP_PATH)
    fun getIpBeanCall(): Call<IpBean>


}