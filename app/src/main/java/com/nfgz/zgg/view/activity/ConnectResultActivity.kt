package com.nfgz.zgg.view.activity

import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.nfgz.zgg.R
import com.nfgz.zgg.ad.AdvertiseManager
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.databinding.ActivityConnectResultBinding
import com.nfgz.zgg.inter.AdShowStateCallBack
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.ProjectUtil
import com.nfgz.zgg.viewmodel.DvViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class ConnectResultActivity : BaseActivity() {
    private lateinit var connectResultDataBinding: ActivityConnectResultBinding
    private lateinit var scopeJob: Job
    override fun getLayoutId(): Int {
        return R.layout.activity_connect_result
    }

    override fun initProcess() {
        connectResultDataBinding = viewDataBinding as ActivityConnectResultBinding
        connectResultDataBinding.lifecycleOwner = this
        connectResultDataBinding.viewModel = DvViewModel

        if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
            connectResultDataBinding.clTitleBar.tvTitle.text = getString(R.string.connect_success)
            connectResultDataBinding.ivIcon.setImageResource(R.mipmap.ic_nfgz_vpn_connect_success)
            connectResultDataBinding.tvVpnConnectState.text =
                getString(R.string.connected_successfully)
        } else if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.STOPPED) {
            connectResultDataBinding.clTitleBar.tvTitle.text = getString(R.string.disconnected)
            connectResultDataBinding.ivIcon.setImageResource(R.mipmap.ic_nfgz_app_icon)
            connectResultDataBinding.tvVpnConnectState.text = getString(R.string.vpn_disconnected)
        }

        connectResultDataBinding.clTitleBar.ivBack.clickDelay {
            finish()
        }

        val activityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val bundle = it.data?.extras
                    val intent = Intent()
                    bundle?.let { bd -> intent.putExtras(bd) }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        //产品上可以不跳转，屏蔽掉
//        connectResultDataBinding.clVpn.clickDelay {
//            activityResult.launch(Intent(this, ServiceListActivity::class.java))
//        }

        val countryAndCity = intent.getStringExtra(ConstantUtil.COUNTRY_AND_CITY_KEY)
        val country = ProjectUtil.splitStrGetCountry(countryAndCity)
        country.let {
            connectResultDataBinding.ivSmallAppIcon.setImageResource(
                ProjectUtil.selectCountryIcon(it)
            )
            connectResultDataBinding.tvShowVpn.text = countryAndCity
        }
        showNativeResultAdAndLoadAd()

//        scopeJob = lifecycleScope.launch(Dispatchers.Main) {
//            flow {
//                (0 until 100).forEach() {
//                    delay(1000L)
//                    emit(it)
//                }
//            }.onStart {
//                Timber.d("initProcess()---onStart---当前线程:${Thread.currentThread().name}")
//                if (AdvertiseManager.checkDayLimit() || AdvertiseManager.isAdAvailable(ConstantUtil.AD_SPACE_NATIVE_RESULT)) {
//                    cancel()
//                    Timber.d("initProcess()---onStart---取消")
//                }
//            }.onCompletion {
//                Timber.d("initProcess()---onCompletion---当前线程:${Thread.currentThread().name}")
//                showNativeResultAdAndLoadAd()
//            }.collect {
//                if (AdvertiseManager.isAdAvailable(ConstantUtil.AD_SPACE_NATIVE_RESULT)
//                    || AdvertiseManager.getAdSpaceBean(
//                        ConstantUtil.AD_SPACE_NATIVE_RESULT
//                    )
//                        ?.allReqAdFinished() == true
//                ) {
//                    cancel()
//                    Timber.d("initProcess()---collect---取消")
//                }
//            }
//        }
    }

    private fun showNativeResultAdAndLoadAd() {
        if (AdvertiseManager.checkDayLimit()) {
            connectResultDataBinding.ivAdBg.visibility = View.VISIBLE
            connectResultDataBinding.cdAdViewGroup.visibility = View.INVISIBLE
            return
        }
        if (AdvertiseManager.isAdAvailable(ConstantUtil.AD_SPACE_NATIVE_RESULT)) {
            Timber.d("showNativeAdAndLoadAd()---结果页原生广告有缓存")
            connectResultDataBinding.ivAdBg.visibility = View.INVISIBLE
            connectResultDataBinding.cdAdViewGroup.visibility = View.VISIBLE
            //展示原生广告
            showNativeResultAd()
        } else {
            Timber.d("showNativeAdAndLoadAd()---结果页没有原生广告缓存")
            connectResultDataBinding.ivAdBg.visibility = View.VISIBLE
            connectResultDataBinding.cdAdViewGroup.visibility = View.INVISIBLE
            reqNativeResultAd()
        }
    }

    private fun showNativeResultAd() {
        AdvertiseManager.showAd(
            this@ConnectResultActivity,
            ConstantUtil.AD_SPACE_NATIVE_RESULT, object : AdShowStateCallBack {
                override fun onAdDismiss() {

                }

                override fun onAdShowed() {
                    reqNativeResultAd()
                }

                override fun onAdShowFail() {

                }
            },
            R.layout.layout_native_ad_connect_result,
            connectResultDataBinding.cdAdViewGroup
        )
    }


    private fun reqNativeResultAd() {
        AdvertiseManager.reqAd(ConstantUtil.AD_SPACE_NATIVE_RESULT)
    }

    override fun onStart() {
        super.onStart()
        if (ProjectUtil.isRefreshNativeAd) {
            Timber.d("onStart()---刷新结果页原生广告")
            showNativeResultAdAndLoadAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        scopeJob.cancel()
    }

}