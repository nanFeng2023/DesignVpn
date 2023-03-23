package com.nfgz.zgg.view.activity

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.webkit.WebViewClient
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.databinding.ActivityPrivacyPolicyBinding
import com.nfgz.zgg.util.ConstantUtil

class PrivacyPolicyActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_policy
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initProcess() {
        val privacyPolicyBinding: ActivityPrivacyPolicyBinding =
            viewDataBinding as ActivityPrivacyPolicyBinding
        privacyPolicyBinding.clTitleBarPrivacy.ivBack.clickDelay {
            finish()
        }
        privacyPolicyBinding.clTitleBarPrivacy.ivBack.drawable.setColorFilter(
            resources.getColor(R.color.black),
            PorterDuff.Mode.SRC_ATOP
        )
        //初始化webView配置
        privacyPolicyBinding.webPrivacy.settings.javaScriptEnabled = true
        privacyPolicyBinding.webPrivacy.webViewClient = WebViewClient()
        privacyPolicyBinding.webPrivacy.loadUrl(ConstantUtil.PRIVACY_POLICY_URL)
    }
}