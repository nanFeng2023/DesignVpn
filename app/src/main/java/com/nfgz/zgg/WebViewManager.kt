package com.nfgz.zgg

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout

class WebViewManager {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    fun buildWebView(): WebView {
        webView = WebView(App.appContext)
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        webView.layoutParams = lp
        webView.settings.javaScriptEnabled = true

        val settings = webView.settings
        //缓存模式
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        //渲染优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        //硬件加速
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.allowFileAccess = true
        settings.saveFormData = false
        settings.loadsImagesAutomatically = true
        return webView
    }

    fun getWebView(): WebView {
        return webView
    }

}