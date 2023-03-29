package com.gameacclerator.lightningoptimizer.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdActivity
import com.gameacclerator.lightningoptimizer.App
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.ad.AdvertiseManager
import com.gameacclerator.lightningoptimizer.bean.VpnBean
import com.gameacclerator.lightningoptimizer.databinding.ActivityServiceListBinding
import com.gameacclerator.lightningoptimizer.inter.AdShowStateCallBack
import com.gameacclerator.lightningoptimizer.inter.AppFrontAndBgListener
import com.gameacclerator.lightningoptimizer.net.retrofit.RetrofitUtil
import com.gameacclerator.lightningoptimizer.util.AlertDialogUtil
import com.gameacclerator.lightningoptimizer.util.ConstantUtil
import com.gameacclerator.lightningoptimizer.util.SPUtil
import com.gameacclerator.lightningoptimizer.view.adapter.ServerListAdapter
import com.gameacclerator.lightningoptimizer.viewmodel.DvViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class ServiceListActivity : BaseActivity(), AppFrontAndBgListener {
    private lateinit var serviceListViewDataBinding: ActivityServiceListBinding
    private var destroyAdPageScopeJob: Job? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_service_list
    }

    override fun initProcess() {
        serviceListViewDataBinding = viewDataBinding as ActivityServiceListBinding
        serviceListViewDataBinding.titleBar.tvTitle.text = getString(R.string.server)
        val serverListAdapter = RetrofitUtil.serviceList?.let { ServerListAdapter(it) }
        serviceListViewDataBinding.rv.layoutManager = LinearLayoutManager(this)
        serviceListViewDataBinding.rv.adapter = serverListAdapter
        if (serverListAdapter != null) {
            serverListAdapter.onItemClick = { position ->
                dialogHint(position)
            }
        }
        App.activityLifecycleCallBack.appFrontAndBgListenerList.add(this)
    }

    override fun setClickEvent() {
        serviceListViewDataBinding.titleBar.ivBack.clickDelay {
            onBackPressed()
        }
    }

    private fun dialogHint(position: Int) {
        if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
            AlertDialogUtil().createDialog(
                this@ServiceListActivity, null,
                ConstantUtil.VPN_SWITCH_HINT, "yes",
                { _, _ ->
                    setResultForActivity(position)
                }, "no", null
            )
        } else {
            setResultForActivity(position)
        }
    }

    private fun setResultForActivity(position: Int) {
        //上次选中的国家
        val lastSelectCountry = SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
        SPUtil.putString(ConstantUtil.LAST_SELECT_COUNTRY, lastSelectCountry)
        //保存选中的VPN 格式：国家-城市
        SPUtil.putString(
            ConstantUtil.CUR_SELECT_COUNTRY,
            RetrofitUtil.serviceList?.get(position)?.getVpnShowTitle()
        )

        //页面回传值
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt(ConstantUtil.CUR_SELECT_COUNTRY_LOCATION, position)
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        if (AdvertiseManager.checkDayLimit()) {
            Timber.d("onBackPressed()---广告达到日上限不展示")
            super.onBackPressed()
            return
        }
        if (AdvertiseManager.isAdAvailable(ConstantUtil.AD_SPACE_INTER_IB)) {
            AdvertiseManager.showAd(
                this@ServiceListActivity,
                ConstantUtil.AD_SPACE_INTER_IB,
                object : AdShowStateCallBack {
                    override fun onAdDismiss() {
                        finishPage()
                    }

                    override fun onAdShowed() {

                    }

                    override fun onAdShowFail() {
                        finishPage()
                    }

                })
        } else {
            finishPage()
        }
    }

    private fun finishPage() {
        Timber.d("finishPage()---")
        finish()
        AdvertiseManager.reqAd(ConstantUtil.AD_SPACE_INTER_IB)
    }

    override fun onAppToFront(activity: Activity) {
        Timber.d("onAppToFront()---")
        destroyAdPageScopeJob?.cancel()
    }

    override fun onAppToBackGround(activity: Activity) {
        if (activity is AdActivity || activity is WebViewAdActivity) {
            destroyAdPageScopeJob = lifecycleScope.launch {
                delay(3000L)
                activity.finish()
                Timber.d("processAdPage()---关闭广告页面")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.activityLifecycleCallBack.appFrontAndBgListenerList.remove(this)
    }

}