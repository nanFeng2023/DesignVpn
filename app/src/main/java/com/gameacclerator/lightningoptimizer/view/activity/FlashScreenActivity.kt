package com.gameacclerator.lightningoptimizer.view.activity

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdActivity
import com.gameacclerator.lightningoptimizer.App
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.ad.AdvertiseManager
import com.gameacclerator.lightningoptimizer.bean.AdSpaceBean
import com.gameacclerator.lightningoptimizer.databinding.ActivityFlashScreenBinding
import com.gameacclerator.lightningoptimizer.inter.AdReqResultCallBack
import com.gameacclerator.lightningoptimizer.inter.AdShowStateCallBack
import com.gameacclerator.lightningoptimizer.inter.AppFrontAndBgListener
import com.gameacclerator.lightningoptimizer.util.ConstantUtil
import com.gameacclerator.lightningoptimizer.util.ProjectUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

/*闪屏页面*/
class FlashScreenActivity : BaseActivity(), AdReqResultCallBack, AppFrontAndBgListener {
    private lateinit var scopeJob: Job
    private lateinit var flashViewDataBinding: ActivityFlashScreenBinding
    private var canBack = true
    private var isHotLaunch = false
    private var defaultTime = 100L
    private var adSpaceBean: AdSpaceBean? = null
    private var flashPageIntoBgCountTimeScopeJob: Job? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_flash_screen
    }

    override fun initProcess() {
        Timber.d("initProcess()---")
        flashViewDataBinding = viewDataBinding as ActivityFlashScreenBinding
        //ip限制检测
        ProjectUtil.restrictIpDetect(null)
        App.activityLifecycleCallBack.appFrontAndBgListenerList.add(this@FlashScreenActivity)
        isHotLaunch = intent.getBooleanExtra(ConstantUtil.IS_HOT_LAUNCH, false)
        adSpaceBean = AdvertiseManager.getAdSpaceBean(ConstantUtil.AD_SPACE_OPEN_ON)
        adSpaceBean?.callBack = this@FlashScreenActivity
        ProjectUtil.isRefreshNativeAd = true
        buildScopeJob()
    }

    private fun buildScopeJob() {
        Timber.d("buildScopeJob()---")
        scopeJob = lifecycleScope.launch {
            Timber.d("buildScopeJob()---协程中的线程:${Thread.currentThread().name}")
            flow {
                (0 until 100).forEach {
                    delay(defaultTime)
                    emit(it)
                }
            }.onStart {
                defaultTime = 100L
                canBack = false
                ProjectUtil.isAdPageDestroyEvent = false
                ProjectUtil.flashPageVisibleReload = false
                //如果广告有缓存或者超限
                if (adSpaceBean?.isAdAvailable() == true || AdvertiseManager.checkDayLimit()) {
                    //有缓存改变loading时间为1秒
                    defaultTime = 10L
                    Timber.d("开屏广告有缓存---")
                } else {
                    Timber.d("冷启动、开屏广告无缓存或缓存过期")
                    preReqAd()
                }
            }.onCompletion {
                canBack = true
                AdvertiseManager.showAd(
                    this@FlashScreenActivity,
                    ConstantUtil.AD_SPACE_OPEN_ON,
                    object : AdShowStateCallBack {
                        override fun onAdDismiss() {
                            Timber.d("onAdDismiss()---")
                            gotoHomePage()
                        }

                        override fun onAdShowed() {
                            Timber.d("onAdShowed()---")
                        }

                        override fun onAdShowFail() {
                            Timber.d("onAdShowFail()---")
                            gotoHomePage()
                        }

                    }, R.layout.activity_flash_screen
                )
            }.collect { process ->
                flashViewDataBinding.progressbar.progress = process
            }
        }
    }

    private fun preReqAd() {
        AdvertiseManager.reqAd(ConstantUtil.AD_SPACE_OPEN_ON)
        AdvertiseManager.reqAd(ConstantUtil.AD_SPACE_INTER_CLICK)
        AdvertiseManager.reqAd(ConstantUtil.AD_SPACE_NATIVE_HOME)
    }

    private fun gotoHomePage() {
        if (ProjectUtil.isPageVisible(this)) {
            if (isHotLaunch) {//热启动关闭闪屏页，恢复到之前页面
                Timber.d("gotoHomePage()---关闭页面显示之前页面")
                finish()
            } else {
                Timber.d("gotoHomePage()---跳转到主页")
                val intent = Intent(this@FlashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Timber.d("gotoHomePage()---页面不可见，不能跳转主页")
        }
    }

    override fun onBackPressed() {
        if (!canBack) {
            return
        }
        super.onBackPressed()
    }

    override fun onAdLoadSuccess() {
        Timber.d("onAdLoadSuccess()---")
        defaultTime = 10L
    }

    override fun onAdLoadFail() {
        Timber.d("onAdLoadFail()---")
        defaultTime = 10L
    }

    override fun onRestart() {
        super.onRestart()
        Timber.d("onRestart()---")
        if (ProjectUtil.isAdPageDestroyEvent || ProjectUtil.flashPageVisibleReload) {
            buildScopeJob()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adSpaceBean?.callBack = null
        App.activityLifecycleCallBack.appFrontAndBgListenerList.remove(this@FlashScreenActivity)
    }

    override fun onAppToFront(activity: Activity) {
        Timber.d("onAppToFront()---")
        flashPageIntoBgCountTimeScopeJob?.cancel()
    }

    override fun onAppToBackGround(activity: Activity) {
        flashPageIntoBgCountTimeScopeJob = lifecycleScope.launch {
            delay(3000L)
            //判断超过3秒关闭广告页面
            if (activity is AdActivity || activity is WebViewAdActivity) {
                activity.finish()
                ProjectUtil.isAdPageDestroyEvent = true
                Timber.d("onAppToBackGround()---关闭广告页面")
                return@launch
            }
            ProjectUtil.flashPageVisibleReload = true
        }
    }
}