package com.nfgz.zgg.net.retrofit

import com.nfgz.zgg.App
import com.nfgz.zgg.bean.IpBean
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.net.ReqCallBack
import com.nfgz.zgg.util.ConstantUtil
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

object RetrofitUtil {
    var serviceList: ArrayList<VpnBean>? = null
    val smartServiceList: ArrayList<VpnBean> = ArrayList()
    private fun getApiRetrofit(host: String): ApiRetrofit {
        val retrofit =
            Retrofit.Builder().baseUrl(host).addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ApiRetrofit::class.java)
    }

    fun detectIp(reqCallBack: ReqCallBack<Response<IpBean>>) {
        val apiRetrofit = getApiRetrofit(ConstantUtil.IP_HOST)
        val beanCall: Call<IpBean> = apiRetrofit.getIpBeanCall()
        beanCall.enqueue(object : Callback<IpBean> {
            override fun onResponse(call: Call<IpBean>, response: Response<IpBean>) {
                reqCallBack.onSuccess(response)
            }

            override fun onFailure(call: Call<IpBean>, t: Throwable) {
                reqCallBack.onFail(t)
            }
        })
    }

    fun getServiceListData() {
        try {
            serviceList =
                serviceListDataParse(nativeJsonDataReader(ConstantUtil.NFGZ_DATA).toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("getServiceListData()---服务器列表数据解析异常")
        }
    }

    fun getSmartServiceListData() {
        try {
            smartListDataParse(nativeJsonDataReader("").toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("getSmartServiceListData()---smart服务器列表数据解析异常")
        }
    }


    private fun nativeJsonDataReader(jsonDataName: String): StringBuilder {
        Timber.d("nativeJsonDataReader()")
        val assetManager = App.appContext.assets
        val inputStreamReader = InputStreamReader(assetManager.open(jsonDataName), "UTF-8")
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        val iterator = bufferedReader.lineSequence().iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            stringBuilder.append(line)
        }
        bufferedReader.close()
        inputStreamReader.close()
        return stringBuilder
    }

    private fun serviceListDataParse(data: String): ArrayList<VpnBean> {
        val list = ArrayList<VpnBean>()
        //服务器列表第一个smart服务器
        val firstItemVpnBean = VpnBean()
        firstItemVpnBean.country = ConstantUtil.DEFAULT_SERVICE
        list.add(firstItemVpnBean)
        val jsonArray = JSONArray(data)
        Timber.d("serviceListDataParse()---服务器数据:$jsonArray")
        for (i in 0 until (jsonArray.length())) {
            val obj = jsonArray.optJSONObject(i)
            val account = obj.optString(ConstantUtil.NFGZACCT)
            val port = obj.optInt(ConstantUtil.NFGZPT)
            val pwd = obj.optString(ConstantUtil.NFGZPD)
            val country = obj.optString(ConstantUtil.NFGZCOY)
            val city = obj.optString(ConstantUtil.NFGZCIY)
            val ip = obj.optString(ConstantUtil.NFGZIP)
            val vpnBean = VpnBean()
            vpnBean.pwd = pwd
            vpnBean.account = account
            vpnBean.port = port
            vpnBean.country = country
            vpnBean.city = city
            vpnBean.ip = ip
            list.add(vpnBean)
        }
        return list
    }

    private fun smartListDataParse(smartData: String) {
        val jsonArray = JSONArray(smartData)
        Timber.d("smartListDataParse()---smart服务器数据:$jsonArray")
        val smartCityNameList: ArrayList<String> = ArrayList()
        for (i in 0 until (jsonArray.length())) {
            val city = jsonArray.optString(i)
            city?.let {
                //城市空格处理
                smartCityNameList.add(city.replace(" ", "").lowercase())
            }
        }
        val tempServiceList = ArrayList<VpnBean>()
        serviceList?.let { tempServiceList.addAll(it) }
        if (smartCityNameList.size > 0 && tempServiceList.size > 0) {//smart列表有数据
            Timber.d("smartListDataParse()---开始解析smart服务器数据")
            for (i in 0 until smartCityNameList.size) {
                val cityName: String = smartCityNameList[i]
                for (j in 0 until tempServiceList.size) {
                    if (cityName == tempServiceList[j].city?.
                        replace(" ", "")?.
                        lowercase()) {
                        //这里取真正的服务器列表数据装入进去
                        smartServiceList.add(serviceList!![j])
                        break
                    }
                }
            }
        }
    }

}