package com.nfgz.zgg.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ParseException
import android.net.Uri
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.nfgz.zgg.ActivityManager
import com.nfgz.zgg.App
import com.nfgz.zgg.R
import com.nfgz.zgg.bean.IpBean
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.inter.BusinessProcessCallBack
import com.nfgz.zgg.inter.IPDelayTimeCallBack
import com.nfgz.zgg.net.ReqApiIml
import com.nfgz.zgg.net.ReqCallBack
import com.nfgz.zgg.viewmodel.DvViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.system.exitProcess

object ProjectUtil {
    var isColdLaunch = true

    //是否进行过IP检测
    var isCheckIpHasBeenPerformed = false

    //是否是广告页面销毁事件
    var isAdPageDestroyEvent = false

    var isRefreshNativeAd = false

    var flashPageVisibleReload = false

    fun restrictIpDetect(callBack: BusinessProcessCallBack<Boolean>?) {
        val reqApiIml = ReqApiIml()
        reqApiIml.detectIp(object : ReqCallBack<Response<IpBean>> {
            override fun onSuccess(response: Response<IpBean>?) {
                Timber.d("onSuccess()---isSuccessful:${response?.isSuccessful}")
                if (response?.isSuccessful == true) {
                    val ipBean = response.body()
                    isRestrictCountry(ipBean?.country, callBack)
                }
            }

            override fun onFail(t: Throwable) {
                Timber.d("onFail()---异常:${t.printStackTrace()}")
                val country = Locale.getDefault().country
                isRestrictCountry(country, callBack)
            }
        })
    }

    private fun isRestrictCountry(country: String?, callBack: BusinessProcessCallBack<Boolean>?) {
        Timber.d("isRestrictCountry()---country:$country")
        isCheckIpHasBeenPerformed = true
        val countryUppercase = country?.uppercase()
        val isBelongRestrictCountry =
            ConstantUtil.COUNTRY_HK == countryUppercase ||
                    /*ConstantUtil.COUNTRY_CN == countryUppercase ||*/
                    ConstantUtil.COUNTRY_IR == countryUppercase ||
                    ConstantUtil.COUNTRY_MO == countryUppercase
        DvViewModel.currentVpnBean.isBelongRestrictCountry = isBelongRestrictCountry
        callBack?.onBusinessProcess(isBelongRestrictCountry)
    }

    fun restrictDialog(activity: AppCompatActivity) {
        AlertDialogUtil().createDialog(
            activity, null,
            "Due to the policy reason , this service is not available in your country",
            "confirm",
            { _, _ ->
                exitProcess(0)
            }, null, null
        )
    }

    fun callEmail() {
        val addresses: Array<String> = arrayOf(ConstantUtil.MAIL_ACCOUNT)
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            App.appContext.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(App.appContext, "Contact us ${addresses[0]}", Toast.LENGTH_SHORT).show()
        }
    }

    fun callShare(activity: AppCompatActivity) {//分享文本
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${ConstantUtil.GOOGLE_STORE_URL}${App.appContext.packageName}"
        )
        activity.startActivity(Intent.createChooser(shareIntent, "share"))
    }

    fun openGooglePlay() {
        val playPackage = "com.android.vending"
        val packageName = App.appContext.packageName
        try {
            val parse = Uri.parse("market://details?id=${packageName}")
            val intent = Intent(Intent.ACTION_VIEW, parse)
            intent.`package` = playPackage
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.appContext.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val parse =
                Uri.parse("${ConstantUtil.GOOGLE_STORE_URL}${App.appContext.packageName}")
            val intent = Intent(Intent.ACTION_VIEW, parse)
            intent.`package` = playPackage
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.appContext.startActivity(intent)
        }
    }

    /*获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2*/
    fun getNetworkType(context: Context): Int {
        var netType = 0
        val manager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        if (networkInfo == null || !networkInfo.isAvailable) {
            return 0
        }
        val nType = networkInfo.type
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            val nSubType = networkInfo.subtype
            val telephonyManager: TelephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            netType =
                if (nSubType == TelephonyManager.NETWORK_TYPE_LTE && !telephonyManager.isNetworkRoaming) {
                    //4G
                    4
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || (nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                            && !telephonyManager.isNetworkRoaming)
                ) {
                    //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
                    3
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || (nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                            && !telephonyManager.isNetworkRoaming)
                ) {
                    //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
                    2
                } else {
                    2
                }
        }
        return netType
    }

    suspend fun delayTest(vpnBean: VpnBean, callBack: IPDelayTimeCallBack?, timeout: Int = 1) {
        var delay = -1
        val count = 1//重试次数
        val ip = vpnBean.ip
        val cmd = "/system/bin/ping -c $count -w $timeout $ip"
        withContext(Dispatchers.IO) {
            val r = ping(cmd)
            if (r != null) {
                try {
                    val index: Int = r.indexOf("min/avg/max/mdev")
                    if (index != -1) {
                        val tempInfo: String = r.substring(index + 19)
                        val temps = tempInfo.split("/".toRegex()).toTypedArray()
                        delay = temps[0].toFloat().roundToInt()//min
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.d("delayTest()---测速异常:${e.printStackTrace()}")
                }
                callBack?.onIpDelayTime(vpnBean, delay)
            }
        }
    }

    /*ping命令*/
    private fun ping(cmd: String): String? {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(cmd) //执行ping指令
            val inputStream = process!!.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (null != reader.readLine().also { line = it }) {
                sb.append(line)
                sb.append("\n")
            }
            reader.close()
            inputStream.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
        return null
    }

    fun splitStrGetCountry(city: String?): String {
        return city?.split("-")?.get(0) ?: ConstantUtil.DEFAULT_SERVICE
    }

    //国家icon资源选择
    fun selectCountryIcon(country: String?): Int {
        val countryResId = when (country?.lowercase()) {
            "united states" -> R.mipmap.ic_nfgz_united_states
            "canada" -> R.mipmap.ic_nfgz_canada
            "australia" -> R.mipmap.ic_nfgz_australia
            "belgium" -> R.mipmap.ic_nfgz_belgium
            "brazil" -> R.mipmap.ic_nfgz_brazil
            "united kingdom" -> R.mipmap.ic_nfgz_united_kingdom
            "france" -> R.mipmap.ic_nfgz_france
            "germany" -> R.mipmap.ic_nfgz_germany
            "hong kong" -> R.mipmap.ic_nfgz_hong_kong
            "india" -> R.mipmap.ic_nfgz_india
            "israel" -> R.mipmap.ic_nfgz_israel
            "italy" -> R.mipmap.ic_nfgz_italy
            "japan" -> R.mipmap.ic_nfgz_japan
            "south korea" -> R.mipmap.ic_nfgz_south_korea
            "netherlands" -> R.mipmap.ic_nfgz_netherlands
            "new zealand" -> R.mipmap.ic_nfgz_new_zealand
            "norway" -> R.mipmap.ic_nfgz_norway
            "ireland" -> R.mipmap.ic_nfgz_ireland
            "russian federation" -> R.mipmap.ic_nfgz_russian_federation
            "singapore" -> R.mipmap.ic_nfgz_singapore
            "sweden" -> R.mipmap.ic_nfgz_sweden
            "switzerland" -> R.mipmap.ic_nfgz_switzerland
            "taiwan" -> R.mipmap.ic_nfgz_taiwan
            "turkey" -> R.mipmap.ic_nfgz_turkey
            "united arab emirates" -> R.mipmap.ic_nfgz_united_arab_emirates
            else -> {
                R.mipmap.ic_nfgz_small_app_icon
            }
        }
        return countryResId
    }

    fun isPageVisible(activity: AppCompatActivity): Boolean {
        Timber.d("isPageVisible()---currentActivity:${activity.localClassName}---currentState:${activity.lifecycle.currentState}")
        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) ||
            activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        ) {
            return true
        }
        return false
    }

    fun getTopActivity(): Activity? {
        return ActivityManager.getCurrentActivity()
    }

    @SuppressLint("SimpleDateFormat")
    fun longToYMDHMS(long: Long): String {
        val date = Date(long)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(date)
    }

    /**
     * 判断给定字符串时间是否为今日
     * @param date
     * @return boolean
     */
    fun isToday(date: String?): Boolean {
        var isToday = false
        val time: Date? = toDate(date)
        val today = Date()
        if (time != null) {
            val nowDate: String = dateFormat2.get()?.format(today) ?: ""
            val timeDate: String = dateFormat2.get()?.format(time) ?: ""
            Timber.d("isToday()---nowDate:$nowDate---timeDate:$timeDate")
            if (nowDate == timeDate) {
                isToday = true
            }
        }
        Timber.d("isToday()---isToday:$isToday")
        return isToday
    }

    private fun toDate(date: String?): Date? {
        return try {
            date?.let { dateFormat.get()?.parse(it) }
        } catch (e: ParseException) {
            null
        }
    }

    private val dateFormat: ThreadLocal<SimpleDateFormat?> =
        object : ThreadLocal<SimpleDateFormat?>() {
            @SuppressLint("SimpleDateFormat")
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            }
        }

    private val dateFormat2: ThreadLocal<SimpleDateFormat?> =
        object : ThreadLocal<SimpleDateFormat?>() {
            @SuppressLint("SimpleDateFormat")
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("yyyy-MM-dd")
            }
        }

}