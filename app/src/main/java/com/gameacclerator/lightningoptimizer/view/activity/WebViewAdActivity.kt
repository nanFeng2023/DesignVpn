package com.gameacclerator.lightningoptimizer.view.activity

import com.google.android.gms.ads.AdError
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.ad.AdvertiseManager
import com.gameacclerator.lightningoptimizer.databinding.ActivityWebViewAdBinding
import timber.log.Timber

class WebViewAdActivity : BaseActivity() {
    private lateinit var webViewAdBinding: ActivityWebViewAdBinding
    override fun getLayoutId(): Int {
        return R.layout.activity_web_view_ad
    }

    override fun initProcess() {
        webViewAdBinding = viewDataBinding as ActivityWebViewAdBinding
        val webView = AdvertiseManager.webViewManager?.getWebView()
        if (webView == null) {
            AdvertiseManager.fullScreenCallBack?.onAdFailedToShowFullScreenContent(
                AdError(
                    400,
                    "webView页面为空",
                    ""
                )
            )
            AdvertiseManager.fullScreenCallBack = null
            finish()
        } else {
            Timber.d("initProcess()---展示webView广告")
            webViewAdBinding.fl.addView(webView)
            AdvertiseManager.fullScreenCallBack?.onAdShowedFullScreenContent()
        }
        webViewAdBinding.titleBar.ivBack.clickDelay {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
        webViewAdBinding.fl.removeAllViews()
        AdvertiseManager.fullScreenCallBack?.onAdDismissedFullScreenContent()
        AdvertiseManager.fullScreenCallBack = null
    }
}