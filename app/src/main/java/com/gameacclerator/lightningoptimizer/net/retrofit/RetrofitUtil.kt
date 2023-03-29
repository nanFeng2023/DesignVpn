package com.gameacclerator.lightningoptimizer.net.retrofit

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.gameacclerator.lightningoptimizer.App
import com.gameacclerator.lightningoptimizer.bean.AdBean
import com.gameacclerator.lightningoptimizer.bean.AdDataResult
import com.gameacclerator.lightningoptimizer.bean.IpBean
import com.gameacclerator.lightningoptimizer.bean.VpnBean
import com.gameacclerator.lightningoptimizer.net.ReqCallBack
import com.gameacclerator.lightningoptimizer.util.ConstantUtil
import com.gameacclerator.lightningoptimizer.util.SPUtil
import org.json.JSONArray
import org.json.JSONObject
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
    var smartServiceList: ArrayList<VpnBean> = ArrayList()
    var adDataResult: AdDataResult? = null
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
            var serviceData = SPUtil.getString(ConstantUtil.REMOTE_SERVICE_CONFIG_KEY)
            if (serviceData.isNullOrEmpty()) {
                serviceData = nativeJsonDataReader(ConstantUtil.NFGZ_DATA)
            }
            serviceList = serviceListDataParse(serviceData)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("getServiceListData()---服务器列表数据解析异常")
        }
    }

    fun getSmartServiceListData() {
        try {
            val smartData = SPUtil.getString(ConstantUtil.REMOTE_SMART_CONFIG_KEY)
            smartListDataParse(smartData)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("getSmartServiceListData()---smart服务器列表数据解析异常")
        }
    }


    private fun nativeJsonDataReader(jsonDataName: String): String {
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
        return stringBuilder.toString()
    }

    private fun serviceListDataParse(data: String?): ArrayList<VpnBean> {
        val list = ArrayList<VpnBean>()
        data?.let {
            //服务器列表第一个smart服务器
            val firstItemVpnBean = VpnBean()
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
        }
        return list
    }

    private fun smartListDataParse(smartData: String?) {
        smartData?.let {
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
                        if (cityName == tempServiceList[j].city?.replace(" ", "")?.lowercase()) {
                            //这里取真正的服务器列表数据装入进去
                            smartServiceList.add(serviceList!![j])
                            break
                        }
                    }
                }
            }
        }
    }

    fun loadRemoteConfigureData() {
        try {
            Firebase.remoteConfig.apply {
                fetchAndActivate().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val serviceVpnData = this.getString(ConstantUtil.REMOTE_SERVICE_CONFIG_KEY)
                        SPUtil.putString(ConstantUtil.REMOTE_SERVICE_CONFIG_KEY, serviceVpnData)

                        val smartServiceVpnData =
                            this.getString(ConstantUtil.REMOTE_SMART_CONFIG_KEY)
                        SPUtil.putString(ConstantUtil.REMOTE_SMART_CONFIG_KEY, smartServiceVpnData)

                        val adData = this.getString(ConstantUtil.REMOTE_AD_CONFIG_KEY)
                        SPUtil.putString(ConstantUtil.REMOTE_AD_CONFIG_KEY, adData)
                        Timber.d(
                            "loadRemoteConfigureData()---请求服务器数据成功---vpn服务器数据:$serviceVpnData" +
                                    "---smart服务器数据:$smartServiceVpnData---广告数据:$adData"
                        )
                    } else {
                        Timber.d("loadRemoteConfigureData()---请求服务器数据失败")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("loadRemoteConfigureData()---请求服务器异常:${e.printStackTrace()}")
        }
    }

    fun getAdListData() {
        try {
            var adStr = SPUtil.getString(ConstantUtil.REMOTE_AD_CONFIG_KEY)
            if (adStr.isNullOrEmpty()) {
                //服务器没数据就解析本地数据
                adStr = nativeJsonDataReader(ConstantUtil.NFGZ_AD)
            }
            adDataResult = adListDataParse(adStr)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("getAdListData()---请求广告数据异常:${e.printStackTrace()}")
        }
    }

    private fun adListDataParse(data: String?): AdDataResult {
        val adResult = AdDataResult()
        data?.let { str ->
            val jsonObject = JSONObject(str)
            val dayShowLimit = jsonObject.optInt(ConstantUtil.DAY_SHOW_LIMIT)
            val dayClickLimit = jsonObject.optInt(ConstantUtil.DAY_CLICK_LIMIT)
            adResult.dayShowLimit = dayShowLimit
            adResult.dayClickLimit = dayClickLimit

            val adSpaceOpenOn = jsonObject.optJSONArray(ConstantUtil.AD_SPACE_OPEN_ON)
            adSpaceOpenOn?.let {
                adResult.adHashMap.put(ConstantUtil.AD_SPACE_OPEN_ON, parseAdItemData(it))
            }

            val adSpaceInterClick = jsonObject.optJSONArray(ConstantUtil.AD_SPACE_INTER_CLICK)
            adSpaceInterClick?.let {
                adResult.adHashMap.put(ConstantUtil.AD_SPACE_INTER_CLICK, parseAdItemData(it))
            }

            val adSpaceInterIb = jsonObject.optJSONArray(ConstantUtil.AD_SPACE_INTER_IB)
            adSpaceInterIb?.let {
                adResult.adHashMap.put(ConstantUtil.AD_SPACE_INTER_IB, parseAdItemData(it))
            }

            val adSpaceNativeHome = jsonObject.optJSONArray(ConstantUtil.AD_SPACE_NATIVE_HOME)
            adSpaceNativeHome?.let {
                adResult.adHashMap.put(ConstantUtil.AD_SPACE_NATIVE_HOME, parseAdItemData(it))
            }

            val adSpaceNativeResult = jsonObject.optJSONArray(ConstantUtil.AD_SPACE_NATIVE_RESULT)
            adSpaceNativeResult?.let {
                adResult.adHashMap.put(ConstantUtil.AD_SPACE_NATIVE_RESULT, parseAdItemData(it))
            }
        }
        return adResult
    }

    private fun parseAdItemData(optJSONArray: JSONArray): ArrayList<AdBean> {
        val adBeanList: ArrayList<AdBean> = ArrayList()
        for (i in 0 until (optJSONArray.length())) {
            val obj = optJSONArray.optJSONObject(i)
            val id = obj.optString(ConstantUtil.AD_ITEM_ID)
            val source = obj.optString(ConstantUtil.AD_ITEM_SOURCE)
            val type = obj.optString(ConstantUtil.AD_ITEM_TYPE)
            val p = obj.optInt(ConstantUtil.AD_ITEM_P)
            val adBean = AdBean()
            adBean.id = id
            adBean.source = source
            adBean.type = type
            adBean.priority = p
            adBeanList.add(adBean)
        }
        return adBeanList
    }

}