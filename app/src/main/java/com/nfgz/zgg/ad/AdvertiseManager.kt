package com.nfgz.zgg.ad

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.nfgz.zgg.App
import com.nfgz.zgg.R
import com.nfgz.zgg.WebViewManager
import com.nfgz.zgg.bean.AdBean
import com.nfgz.zgg.bean.AdDataResult
import com.nfgz.zgg.bean.AdSpaceBean
import com.nfgz.zgg.inter.AdShowStateCallBack
import com.nfgz.zgg.net.retrofit.RetrofitUtil
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.ProjectUtil
import com.nfgz.zgg.util.SPUtil
import com.nfgz.zgg.view.activity.WebViewAdActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

/*广告管理类*/
object AdvertiseManager {
    private var adSpaceHashMap: HashMap<String, AdSpaceBean> = HashMap()
    private var adDataResult: AdDataResult? = null
    var fullScreenCallBack: FullScreenContentCallback? = null

    @SuppressLint("StaticFieldLeak")
    var webViewManager: WebViewManager? = null

    init {
        adSpaceHashMap[ConstantUtil.AD_SPACE_OPEN_ON] = AdSpaceBean(ConstantUtil.AD_SPACE_OPEN_ON)
        adSpaceHashMap[ConstantUtil.AD_SPACE_INTER_CLICK] =
            AdSpaceBean(ConstantUtil.AD_SPACE_INTER_CLICK)
        adSpaceHashMap[ConstantUtil.AD_SPACE_INTER_IB] = AdSpaceBean(ConstantUtil.AD_SPACE_INTER_IB)
        adSpaceHashMap[ConstantUtil.AD_SPACE_NATIVE_HOME] =
            AdSpaceBean(ConstantUtil.AD_SPACE_NATIVE_HOME)
        adSpaceHashMap[ConstantUtil.AD_SPACE_NATIVE_RESULT] =
            AdSpaceBean(ConstantUtil.AD_SPACE_NATIVE_RESULT)
    }

    /*请求广告入口*/
    fun reqAd(adSpaceType: String) {
        Timber.d("reqAd()---")
        adDataResult = RetrofitUtil.adDataResult ?: return
        val adSpaceBean = adSpaceHashMap[adSpaceType]
        if (checkDayLimit()) {
            Timber.d("reqAd()---已达到当日广告展示上限或点击上限")
            adSpaceBean?.callBack?.onAdLoadSuccess()
            return
        }
        adSpaceBean?.apply {
            if (adCanUse(adSpaceBean)) return
            val adBeanList = adDataResult!!.adHashMap[adSpaceType]
            if (adBeanList != null) {
                //优先级排序
                priorityQuickSort(adBeanList, 0, adBeanList.size - 1)
                adSpaceBean.adBeanList.clear()
                adSpaceBean.adBeanList.addAll(adBeanList)
                reqAd(adSpaceType, adSpaceBean)
            }
        }
    }

    private fun adCanUse(adSpaceBean: AdSpaceBean): Boolean {
        adSpaceBean.apply {
            if (adSpaceBean.requesting || isAdAvailable()) {
                if (adSpaceBean.requesting) {
                    Timber.d("adCanUse()---当前有广告正在请求---广告位类型:$spaceType---子广告类型:$itemAdType")
                }
                if (ad != null) {
                    Timber.d("adCanUse()---当前有广告缓存---广告位类型:$spaceType---子广告类型:$itemAdType")
                }
                if (!isAdNotExpiration()) {
                    Timber.d("adCanUse()---当前广告没有过期---广告位类型:$spaceType---子广告类型:$itemAdType")
                }
                return true
            }
        }
        return false
    }

    fun checkDayLimit(): Boolean {
        Timber.d("checkDayLimit()---")
        var lastAdRecordTime = SPUtil.getString(ConstantUtil.LAST_AD_RECORD_TIME)
        if (lastAdRecordTime.isNullOrEmpty()) {//记录第一次广告展示时间
            lastAdRecordTime = ProjectUtil.longToYMDHMS(System.currentTimeMillis())
            SPUtil.putString(ConstantUtil.LAST_AD_RECORD_TIME, lastAdRecordTime)
        }
        var dayShowLimit = SPUtil.getInt(ConstantUtil.AD_SHOW_DAY_LIMIT_KEY)
        var dayClickLimit = SPUtil.getInt(ConstantUtil.AD_CLICK_DAY_LIMIT_KEY)
        val timeIsToday = ProjectUtil.isToday(lastAdRecordTime)
        val isOverDayLimit = if (timeIsToday) {
            dayShowLimit >= RetrofitUtil.adDataResult?.dayShowLimit!! || dayClickLimit >= RetrofitUtil.adDataResult?.dayClickLimit!!
        } else {
            dayShowLimit = 0
            dayClickLimit = 0
            SPUtil.putString(
                ConstantUtil.LAST_AD_RECORD_TIME,
                ProjectUtil.longToYMDHMS(System.currentTimeMillis())
            )
            SPUtil.putInt(ConstantUtil.AD_SHOW_DAY_LIMIT_KEY, dayShowLimit)
            SPUtil.putInt(ConstantUtil.AD_CLICK_DAY_LIMIT_KEY, dayClickLimit)
            false
        }
        return isOverDayLimit
    }

    //优先级快速排序
    private fun priorityQuickSort(list: ArrayList<AdBean>, left: Int, right: Int) {
        if (left > right) return
        //base存放基准数
        val base = list[left]
        var i = left
        var j = right
        while (i != j) {
            //先从右开始往左边找，直到找到比base值大的数
            while (list[j].priority <= base.priority && i < j) {
                j--
            }
            //再从左往右边找，直到找到base值小的数
            while (list[i].priority >= base.priority && i < j) {
                i++
            }
            //上面循环结束表示找到了位置或者(i>j)了，交换两个数在数组中的位置
            if (i < j) {
                val temp = list[i]
                list[i] = list[j]
                list[j] = temp
            }
        }
        //将基准数放到中间位置
        list[left] = list[i]
        list[i] = base
        priorityQuickSort(list, left, i - 1)
        priorityQuickSort(list, i + 1, right)
    }

    private fun reqAd(adSpaceType: String, adSpaceBean: AdSpaceBean) {
        val iterator = adSpaceBean.adBeanList.iterator()
        if (iterator.hasNext()) {
            val adBean = iterator.next()
            adSpaceBean.itemAdType = adBean.type
            iterator.remove()
            when (adSpaceType) {
                ConstantUtil.AD_SPACE_OPEN_ON -> {
                    when (adSpaceBean.itemAdType) {
                        ConstantUtil.AD_TYPE_OPEN -> {
                            reqOpenAd(adBean, adSpaceBean)
                        }
                        ConstantUtil.AD_TYPE_INTER -> {
                            reqInterAd(adBean, adSpaceBean)
                        }
                        else -> {
                            printAdItemNotCase(adSpaceType, adSpaceBean)
                        }
                    }
                }

                ConstantUtil.AD_SPACE_INTER_CLICK -> {
                    if (adSpaceBean.itemAdType == ConstantUtil.AD_TYPE_INTER) {
                        reqInterAd(adBean, adSpaceBean)
                    } else {
                        printAdItemNotCase(adSpaceType, adSpaceBean)
                    }
                }

                ConstantUtil.AD_SPACE_INTER_IB -> {
                    if (adSpaceBean.itemAdType == ConstantUtil.AD_TYPE_INTER) {
                        reqInterAd(adBean, adSpaceBean)
                    } else {
                        printAdItemNotCase(adSpaceType, adSpaceBean)
                    }
                }

                ConstantUtil.AD_SPACE_NATIVE_HOME -> {
                    if (adSpaceBean.itemAdType == ConstantUtil.AD_TYPE_NATIVE) {
                        reqNativeAd(adBean, adSpaceBean)
                    } else {
                        printAdItemNotCase(adSpaceType, adSpaceBean)
                    }
                }

                ConstantUtil.AD_SPACE_NATIVE_RESULT -> {
                    if (adSpaceBean.itemAdType == ConstantUtil.AD_TYPE_NATIVE) {
                        reqNativeAd(adBean, adSpaceBean)
                    } else {
                        printAdItemNotCase(adSpaceType, adSpaceBean)
                    }
                }
            }
        }

    }

    private fun printAdItemNotCase(adSpaceType: String, adSpaceBean: AdSpaceBean) {
        Timber.d("reqAd()---广告Item类型不对---广告类类型:$adSpaceType---子广告类型:${adSpaceBean.itemAdType}")
    }

    private fun reqOpenAd(adBean: AdBean, adSpaceBean: AdSpaceBean) {
        Timber.d("reqOpenAd()---广告ID:${adBean.id}")
        adBean.id?.let { id ->
            Timber.d("reqOpenAd()---开始请求广告:${adSpaceBean.spaceType}---优先级:${adBean.priority}---广告ID:${adBean.id}")
            if (adBean.source == ConstantUtil.AD_SOURCE_H5) {
                adSpaceBean.ad = id
                createWebView(adSpaceBean)
            } else if (adBean.source == ConstantUtil.AD_SOURCE_ADMOB) {
                val request = AdRequest.Builder().build()
                adSpaceBean.requesting = true
                AppOpenAd.load(App.appContext,
                    id,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            Timber.d("reqOpenAd()---广告类类型:${adSpaceBean.spaceType}---onAdLoaded()")
                            adLoadedSetState(ad, adSpaceBean)
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Timber.d("reqOpenAd()---广告类类型:${adSpaceBean.spaceType}---onAdFailedToLoad():$loadAdError")
                            adLoadFailSetState(adSpaceBean)
                            reReqAd(adSpaceBean)
                        }
                    })
            }
        }
    }

    private fun reReqAd(adSpaceBean: AdSpaceBean) {
        Timber.d("reReqAd()---")
        if (adSpaceBean.adBeanList.iterator().hasNext()) {
            Timber.d("reReqAd()---取次优先级广告---广告类型:${adSpaceBean.spaceType}")
            //取低优先级
            reqAd(adSpaceBean.spaceType!!, adSpaceBean)
        } else {
            if (adSpaceBean.spaceType == ConstantUtil.AD_SPACE_OPEN_ON) {
                if (adSpaceBean.canReload) {
                    adSpaceBean.canReload = false
                    adDataResult?.adHashMap?.get(adSpaceBean.spaceType)?.let { list ->
                        Timber.d("reReqAd()---广告类类型:${adSpaceBean.spaceType}---再次请求开屏广告")
                        adSpaceBean.adBeanList.clear()
                        adSpaceBean.adBeanList.addAll(list)
                        reqAd(adSpaceBean.spaceType!!, adSpaceBean)
                    }
                } else {
                    Timber.d("reReqAd()---广告类类型:${adSpaceBean.spaceType}---再次请求开屏广告失败")
                    adSpaceBean.callBack?.onAdLoadFail()
                }
            }
        }
    }

    private fun adLoadFailSetState(adSpaceBean: AdSpaceBean) {
        adSpaceBean.requesting = false
        adSpaceBean.ad = null
    }

    private fun adLoadedSetState(ad: Any, adSpaceBean: AdSpaceBean) {
        adSpaceBean.ad = ad
        adSpaceBean.requesting = false
        adSpaceBean.expirationTime = Date().time
        adSpaceBean.callBack?.onAdLoadSuccess()
    }

    private fun reqInterAd(adBean: AdBean, adSpaceBean: AdSpaceBean) {
        Timber.d("reqInterAd()---广告ID:${adBean.id}")
        adBean.id?.let { id ->
            Timber.d("reqInterAd()---开始请求广告:${adSpaceBean.spaceType}---优先级:${adBean.priority}---广告ID:${adBean.id}")
            if (adBean.source == ConstantUtil.AD_SOURCE_H5) {
                adSpaceBean.ad = id
                createWebView(adSpaceBean)
            } else if (adBean.source == ConstantUtil.AD_SOURCE_ADMOB) {
                adSpaceBean.requesting = true
                val request = AdRequest.Builder().build()
                InterstitialAd.load(App.appContext,
                    id,
                    request,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            Timber.d("reqInterAd()---广告类类型:${adSpaceBean.spaceType}---onAdLoaded()")
                            adLoadedSetState(interstitialAd, adSpaceBean)
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Timber.d("reqInterAd()---广告类类型:${adSpaceBean.spaceType}---onAdFailedToLoad():$loadAdError")
                            adLoadFailSetState(adSpaceBean)
                            reReqAd(adSpaceBean)
                        }
                    })
            }
        }
    }

    private fun reqNativeAd(adBean: AdBean, adSpaceBean: AdSpaceBean) {
        Timber.d("reqNativeAd()---")
        adBean.id?.let { id ->
            Timber.d("reqNativeAd()---开始请求广告:${adSpaceBean.spaceType}---优先级:${adBean.priority}---广告ID:${adBean.id}")
            adSpaceBean.requesting = true
            val adLoader = AdLoader.Builder(App.appContext, id).forNativeAd { nativeAd ->
                Timber.d("reqNativeAd()---原生广告${adSpaceBean.spaceType}请求成功---nativeAd:$nativeAd")
                adLoadedSetState(nativeAd, adSpaceBean)
            }.withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    adClickStatistic()
                }

                override fun onAdFailedToLoad(loadError: LoadAdError) {
                    Timber.d("reqNativeAd()---原生广告${adSpaceBean.spaceType}请求失败---onAdFailedToLoad():$loadError")
                    adLoadFailSetState(adSpaceBean)
                    reReqAd(adSpaceBean)
                }
            }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    fun showAd(
        activity: AppCompatActivity,
        adSpaceType: String,
        adShowStateCallBack: AdShowStateCallBack?,
        layoutId: Int? = 0,
        viewGroup: ViewGroup? = null
    ) {
        Timber.d("showAd()---")
        activity.lifecycleScope.launch(Dispatchers.Main.immediate) {
            delay(50L)
            if (ProjectUtil.isPageVisible(activity)) {//页面可见
                val adSpaceBean = adSpaceHashMap[adSpaceType]
                adSpaceBean?.apply {
                    if (!isAdAvailable() || checkDayLimit()) {
                        if (!isAdAvailable()) {
                            Timber.d("showAd()---广告类型:$adSpaceType---没有缓存或过期")
                        } else if (checkDayLimit()) {
                            Timber.d("showAd()---广告超限")
                        }
                        adShowStateCallBack?.onAdShowFail()
                        return@launch
                    }
                    Timber.d("showAd()---广告类型:$adSpaceType---展示广告---")
                    fullScreenCallBack = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {//该回调是点击广告关闭时触发
                            Timber.d("showAd()---广告类型:$adSpaceType---onAdDismissedFullScreenContent()")
                            adShowStateCallBack?.onAdDismiss()
                            fullScreenCallBack = null
                            if (webViewManager != null) {
                                webViewManager = null
                            }
                        }

                        override fun onAdShowedFullScreenContent() {
                            Timber.d("showAd()---广告类型:$adSpaceType---onAdShowedFullScreenContent()")
                            adSpaceBean.ad = null
                            adShowStateCallBack?.onAdShowed()
                            adShowStatistic()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            Timber.d("showAd()---广告类型:$adSpaceType---onAdFailedToShowFullScreenContent():$p0")
                            adSpaceBean.ad = null
                            adShowStateCallBack?.onAdShowFail()
                            fullScreenCallBack = null
                            if (webViewManager != null) {
                                webViewManager = null
                            }
                        }

                        override fun onAdClicked() {
                            Timber.d("showAd()---广告类型:$adSpaceType---onAdClicked()")
                            adClickStatistic()
                        }
                    }
                    when (adSpaceBean.ad) {
                        is AppOpenAd -> {
                            (ad as AppOpenAd).fullScreenContentCallback = fullScreenCallBack
                            (ad as AppOpenAd).show(activity)
                        }
                        is InterstitialAd -> {
                            (ad as InterstitialAd).fullScreenContentCallback = fullScreenCallBack
                            (ad as InterstitialAd).show(activity)
                        }
                        is NativeAd -> {
                            createNativeAdView(
                                activity,
                                layoutId,
                                ad as NativeAd,
                                viewGroup,
                                adSpaceBean,
                                adShowStateCallBack
                            )
                        }
                        is String -> {//h5链接
                            Timber.d("showAd()---h5链接加载完成，跳转webView页面")
                            val intent = Intent(activity, WebViewAdActivity::class.java)
                            activity.startActivity(intent)
                            activity.overridePendingTransition(0, 0)
                        }
                        else -> {
                            Timber.d("showAd()---no such ad")
                            adShowStateCallBack?.onAdShowFail()
                        }
                    }
                }
            } else {
                Timber.d("showAd()---页面不可见")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(adSpaceBean: AdSpaceBean) {
        Timber.d("createWebView()---")
        //webView
        webViewManager = WebViewManager()
        val webView = webViewManager?.buildWebView()
        webView?.webViewClient = object : WebViewClient() {
            var isPageFinished = false
            var isLoadError = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Timber.d("onPageStarted()---url:$url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("onPageFinished()---url:$url")
                isPageFinished = true
                if (!isLoadError) {
                    adSpaceBean.ad?.let {
                        adLoadedSetState(it, adSpaceBean)
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Timber.d("onReceivedError()---error:${error?.description}")
                isLoadError = true
                adLoadFailSetState(adSpaceBean)
                reReqAd(adSpaceBean)
                webViewManager = null
            }
        }
        val url: String = adSpaceBean.ad.toString()
        Timber.d("createWebView()---url:$url")
        webView?.loadUrl(url)
    }

    private fun createNativeAdView(
        activity: AppCompatActivity,
        layoutId: Int?,
        nativeAd: NativeAd,
        nativeAdParentGroup: ViewGroup?,
        adSpaceBean: AdSpaceBean,
        adShowStateCallBack: AdShowStateCallBack?
    ) {
        val nativeAdView =
            layoutId?.let { activity.layoutInflater.inflate(it, null) } as NativeAdView
        nativeAdView.run {
            mediaView = findViewById(R.id.mv_ad)
            callToActionView = findViewById<TextView>(R.id.tv_ad_call_to_action)
            headlineView = findViewById<TextView>(R.id.tv_ad_headline)
            iconView = findViewById<ImageView>(R.id.iv_ad_icon)
            advertiserView = findViewById<TextView>(R.id.tv_ad_advertiser)
            nativeAd.mediaContent?.let {
                mediaView?.mediaContent = it
            }
            (headlineView as TextView?)?.text = nativeAd.headline
            (advertiserView as TextView?)?.text = nativeAd.advertiser
            if (nativeAd.icon == null) {
                (iconView as ImageView?)?.visibility = View.INVISIBLE
            } else {
                (iconView as ImageView?)?.setImageDrawable(nativeAd.icon?.drawable)
                (iconView as ImageView?)?.visibility = View.VISIBLE
            }
            if (nativeAd.callToAction == null) {
                (callToActionView as TextView?)?.visibility = View.INVISIBLE
            } else {
                (callToActionView as TextView?)?.visibility = View.VISIBLE
                (callToActionView as TextView?)?.text = nativeAd.callToAction
            }

            nativeAdView.setNativeAd(nativeAd)
            nativeAdParentGroup?.removeAllViews()
            nativeAdParentGroup?.addView(nativeAdView)
            activity.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        activity.lifecycle.removeObserver(this)
                        nativeAdView.destroy()
                    }
                }
            })
            adSpaceBean.ad = null
            adShowStatistic()
            adShowStateCallBack?.onAdShowed()
        }
    }

    /*广告显示统计*/
    private fun adShowStatistic() {
        var showNum: Int = SPUtil.getInt(ConstantUtil.AD_SHOW_DAY_LIMIT_KEY)
        SPUtil.putInt(ConstantUtil.AD_SHOW_DAY_LIMIT_KEY, ++showNum)
        Timber.d("adShowStatistic()---广告展示---showNum:$showNum")
    }

    /*广告点击统计*/
    private fun adClickStatistic() {
        var clickNum: Int = SPUtil.getInt(ConstantUtil.AD_CLICK_DAY_LIMIT_KEY)
        SPUtil.putInt(ConstantUtil.AD_CLICK_DAY_LIMIT_KEY, ++clickNum)
        Timber.d("adClickStatistic()---广告点击---clickNum:$clickNum")
    }

    fun isAdAvailable(adSpaceType: String): Boolean {
        return adSpaceHashMap[adSpaceType]?.isAdAvailable() ?: false
    }

    fun getAdSpaceBean(adSpaceType: String): AdSpaceBean? {
        return adSpaceHashMap[adSpaceType]
    }

}