package com.nfgz.zgg.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.RemoteException
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.databinding.ActivityMainBinding
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.nfgz.zgg.App
import com.nfgz.zgg.PermissionVPN
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.inter.AppFrontAndBgListener
import com.nfgz.zgg.inter.BusinessProcessCallBack
import com.nfgz.zgg.inter.IPDelayTimeCallBack
import com.nfgz.zgg.net.retrofit.RetrofitUtil
import com.nfgz.zgg.util.*
import com.nfgz.zgg.viewmodel.DvViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), ShadowsocksConnection.Callback {
    private lateinit var guildLayout: ConstraintLayout
    private lateinit var guideLav: LottieAnimationView
    private lateinit var mainDataBinding: ActivityMainBinding
    private val shadowSockConnection = ShadowsocksConnection(true)
    private val activityResult = registerForActivityResult(PermissionVPN()) {
        if (it) {//权限拒绝

        } else {//权限允许
            connectPermissionDetect()
        }
    }
    private lateinit var connectJob: Job

    private lateinit var currentVpnBean: VpnBean
    private var curSelectCountry: String? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var isServerPageReqStopVpn = false

    private fun connectPermissionDetect() {
        Timber.d("connectPermissionDetect()")
        if (netCheck())
            return
        if (!ProjectUtil.isCheckIpHasBeenPerformed) {//没有检测IP就去检测
            ProjectUtil.restrictIpDetect(object : BusinessProcessCallBack<Boolean> {
                override fun onBusinessProcess(isRetricArea: Boolean) {
                    if (isRetricArea) {
                        ProjectUtil.restrictDialog(this@MainActivity)
                    } else {
                        startVpnConnect()
                    }
                }

            })
        } else {
            if (ipCheck())//检测过IP并且是限制地区则提示
                return
            startVpnConnect()
        }
    }

    private fun startVpnConnect() {
        Timber.d("startVpnConnect()")
        curSelectCountry = SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
        if (curSelectCountry == ConstantUtil.DEFAULT_SERVICE) {
            try {
                selectSmartService()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("startVpnConnect()---smart测速异常:${e.printStackTrace()}")
            }
        }
        connectJob = lifecycleScope.launch {
            flow {
                repeat((0 until 5).count()) {
                    delay(1000)
                    emit(it)
                }
            }.onStart {
                connectStateIvBgDismiss()
                mainDataBinding.lav.playAnimation()
                maskClickEvent(false)
                currentVpnBean.setVpnState(VpnBean.VpnState.CONNECTING)
            }.onCompletion {
                if (currentVpnBean.state == VpnBean.VpnState.CONNECTING) {
                    Timber.d("startVpnConnect()---开始连接VPN")
                    Core.startService()
                }
            }.collect {

            }
        }
    }

    private fun stopVpnConnected() {
        Timber.d("stopVpnConnected()")
        connectJob = lifecycleScope.launch {
            flow {
                repeat((0 until 5).count()) {
                    delay(1000)
                    emit(it)
                }
            }.onStart {
                connectStateIvBgDismiss()
                mainDataBinding.lav.playAnimation()
                currentVpnBean.setVpnState(VpnBean.VpnState.STOPPING)
            }.onCompletion {
                if (currentVpnBean.state == VpnBean.VpnState.STOPPING) {
                    Timber.d("stopVpnConnected()---开始停止VPN")
                    Core.stopService()
                }
            }.collect {

            }
        }
    }

    /*屏蔽点击事件*/
    private fun maskClickEvent(isClickable: Boolean) {
        mainDataBinding.lav.isClickable = isClickable
        mainDataBinding.clVpn.root.isClickable = isClickable
        mainDataBinding.ivSet.isClickable = isClickable
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initProcess() {
        mainDataBinding = viewDataBinding as ActivityMainBinding
        mainDataBinding.lifecycleOwner = this
        //初始VPN对象
        DvViewModel.initVpnState()
        currentVpnBean = DvViewModel.currentVpnBean
        //vpn对象设置到布局中
        mainDataBinding.viewModel = DvViewModel
        mainDataBinding.clVpn.viewModel = DvViewModel

        //网络检测
        netCheck()
        //ip检测
        if (ipCheck())
            return
        //连接成功状态显示背景图
        if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
            Timber.d("onCreate()---显示背景图")
            connectStateIvBgShow()
        }
        //显示引导页
        showGuildAnimation()
        changeVpnState(BaseService.State.Idle)
        shadowSockConnection.connect(this, this)

        //前后台监听
        App.activityLifecycleCallBack.appFrontAndBgListener = object : AppFrontAndBgListener {
            override fun onAppToFront() {

            }

            override fun onAppToBackGround() {
                stopAnimationAndResetState()
            }
        }

        //首次进app默认选择smart
        val isFirstIntoApp = SPUtil.getBoolean(ConstantUtil.IS_FIRST_INTO_APP, true)
        if (isFirstIntoApp) {
            SPUtil.putBoolean(ConstantUtil.IS_FIRST_INTO_APP, false)
            SPUtil.putString(ConstantUtil.CUR_SELECT_COUNTRY, ConstantUtil.DEFAULT_SERVICE)
        }

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val bundle = it.data?.extras
                    val position = bundle?.getInt(ConstantUtil.CUR_SELECT_COUNTRY_LOCATION)
                    val servicePageSelectVpnBean = RetrofitUtil.serviceList?.get(position!!)
                    Timber.d("registerForActivityResult()---更新服务器列表选择的VPN对象")
                    servicePageSelectVpnBean?.let { bean ->
                        DvViewModel.remainLastVpnBeanInfo()
                        DvViewModel.updateVpnBean(bean)
                        DvViewModel.updateProfile(bean)
                    }

                    if (currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
                        isServerPageReqStopVpn = true
                        stopVpnConnected()
                    } else {
                        activityResult.launch(null)
                    }
                }
            }
        //抽屉布局禁止手势
        mainDataBinding.dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun connectStateIvBgShow() {
        mainDataBinding.ivConnectStateBg.visibility = View.VISIBLE
        mainDataBinding.ivConnectStateBg.setImageResource(R.mipmap.ic_nfgz_vpn_connect_success)
        mainDataBinding.ivBg.visibility = View.INVISIBLE
        mainDataBinding.lav.visibility = View.INVISIBLE
    }

    private fun connectStateIvBgDismiss() {
        mainDataBinding.ivConnectStateBg.visibility = View.INVISIBLE
        mainDataBinding.ivBg.visibility = View.VISIBLE
        mainDataBinding.lav.visibility = View.VISIBLE
    }

    private fun cancelAnimation() {
        mainDataBinding.lav.cancelAnimation()
        maskClickEvent(true)
    }

    private fun stopAnimationAndResetState() {
        Timber.d("stopAnimationAndResetState()---currentVpnBean.state:${currentVpnBean.state}")
        if (currentVpnBean.state == VpnBean.VpnState.CONNECTING) {
            currentVpnBean.setVpnState(VpnBean.VpnState.IDLE)
            mainDataBinding.lav.progress = 0f
            cancelAnimation()
            connectJob.cancel()
            Timber.d("stopAnimationAndResetState()---停止vpn连接动画")
            //上次选中的国家取出来重新保存
            val lastSelectCountry = SPUtil.getString(ConstantUtil.LAST_SELECT_COUNTRY)
            SPUtil.putString(ConstantUtil.CUR_SELECT_COUNTRY, lastSelectCountry)
            DvViewModel.updateVpnBean(DvViewModel.resetVpnBean)
            DvViewModel.updateProfile(DvViewModel.resetVpnBean)
        }

        if (currentVpnBean.state == VpnBean.VpnState.STOPPING) {
            currentVpnBean.setVpnState(VpnBean.VpnState.CONNECTED)
            mainDataBinding.lav.progress = 1f
            cancelAnimation()
            connectJob.cancel()
            connectStateIvBgShow()
            Timber.d("stopAnimationAndResetState()---停止vpn关闭动画")
            //上次选中的国家取出来重新保存
            val lastSelectCountry = SPUtil.getString(ConstantUtil.LAST_SELECT_COUNTRY)
            SPUtil.putString(ConstantUtil.CUR_SELECT_COUNTRY, lastSelectCountry)
            DvViewModel.updateVpnBean(DvViewModel.resetVpnBean)
            DvViewModel.updateProfile(DvViewModel.resetVpnBean)
        }
    }

    override fun setClickEvent() {
        //点击设置按钮
        mainDataBinding.ivSet.clickDelay {
            mainDataBinding.dl.open()
            stopAnimationAndResetState()
        }
        //抽屉点击选项
        mainDataBinding.nav.setNavigationItemSelectedListener { item ->
            if (item.isChecked) mainDataBinding.dl.closeDrawers() else {
                when (item.itemId) {
                    R.id.contract_us -> {
                        ProjectUtil.callEmail()
                    }
                    R.id.update -> {
                        ProjectUtil.openGooglePlay()
                    }
                    R.id.privacy_policy -> {
                        startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                    }
                    R.id.share -> {
                        ProjectUtil.callShare(this@MainActivity)
                    }
                }
            }
            true
        }

        mainDataBinding.clVpn.root.clickDelay {
            val intent = Intent(this@MainActivity, ServiceListActivity::class.java)
            resultLauncher?.launch(intent)
            stopAnimationAndResetState()
        }

        mainDataBinding.lav.clickDelay {
            Timber.d("setClickEvent()---点击动画按钮")
            clickAnimationBtn()
        }

        mainDataBinding.ivConnectStateBg.clickDelay {
            Timber.d("setClickEvent()---点击连接成功背景按钮")
            mainDataBinding.ivBg.visibility = View.VISIBLE
            mainDataBinding.lav.visibility = View.VISIBLE
            mainDataBinding.ivConnectStateBg.visibility = View.INVISIBLE
            clickAnimationBtn()
        }

    }

    private fun clickAnimationBtn() {
        if (currentVpnBean.state == VpnBean.VpnState.IDLE
            || currentVpnBean.state == VpnBean.VpnState.STOPPED
        )
            activityResult.launch(null)
        else if (currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
            isServerPageReqStopVpn = false
            stopVpnConnected()
        }
    }


    private fun ipCheck(): Boolean {
        if (currentVpnBean.isBelongRestrictCountry) {
            ProjectUtil.restrictDialog(this@MainActivity)
            return true
        }
        return false
    }

    private fun netCheck(): Boolean {
        if (ProjectUtil.getNetworkType(this) == 0) {//无网络
            AlertDialogUtil().createDialog(
                this@MainActivity, null,
                "Network request timed out. Please make sure your network is connected",
                "OK",
                { dialog, _ -> dialog?.dismiss() }, null, null
            )
            return true
        }
        return false
    }

    private fun showGuildAnimation() {
        if (ProjectUtil.isColdLaunch) {
            ProjectUtil.isColdLaunch = false
            val decorView: FrameLayout = window.decorView as FrameLayout
            //父布局
            guildLayout = ConstraintLayout(this@MainActivity)
            val lp =
                ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            guildLayout.layoutParams = lp
            guildLayout.setBackgroundColor(Color.parseColor("#B3000000"))
            guildLayout.isClickable = true

            //引导动画
            guideLav = LottieAnimationView(this@MainActivity)
            guideLav.imageAssetsFolder = ConstantUtil.GUIDE_IMAGE_ASSETS_FOlDER
            guideLav.setAnimation(ConstantUtil.GUIDE_ANIMATION_JSON)
            guideLav.loop(true)
            guideLav.playAnimation()
            val params = ConstraintLayout.LayoutParams(
                StatusBarUtil.dip2px(this, 256f), StatusBarUtil.dip2px(this, 256f)
            )
            guideLav.scaleType = ImageView.ScaleType.FIT_XY
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.topMargin =
                StatusBarUtil.dip2px(this, 225f) +
                        StatusBarUtil.getStatusBarHeight(this).toInt()
            guideLav.layoutParams = params
            guildLayout.addView(guideLav)
            //加入布局
            decorView.addView(guildLayout)
            guideLav.clickDelay {
                dismissGuildAnimation()
                activityResult.launch(null)
            }
            currentVpnBean.isShowingGuide = true
        }
    }

    private fun dismissGuildAnimation() {
        guildLayout.clearAnimation()
        (window.decorView as FrameLayout).removeView(guildLayout)
        currentVpnBean.isShowingGuide = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (currentVpnBean.isShowingGuide) {
            dismissGuildAnimation()
            return true
        } else if (currentVpnBean.state == VpnBean.VpnState.CONNECTING) {
            return true
        } else if (currentVpnBean.state == VpnBean.VpnState.STOPPING) {
            stopAnimationAndResetState()
            return true
        } else if (mainDataBinding.dl.isOpen) {
            mainDataBinding.dl.close()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Timber.d("stateChanged()---state:$state---profileName:$profileName")
        changeVpnState(state)
        vpnConnectedOrStopped(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) = changeVpnState(
        try {
            Timber.d("onServiceConnected()---service.state:${service.state}")
            BaseService.State.values()[service.state]
        } catch (_: RemoteException) {
            BaseService.State.Idle
        }
    )

    override fun onServiceDisconnected() {
        Timber.d("onServiceDisconnected()")
        changeVpnState(BaseService.State.Idle)
        Toast.makeText(this@MainActivity, "please try again", Toast.LENGTH_LONG).show()
        if (mainDataBinding.lav.isAnimating) {
            mainDataBinding.lav.cancelAnimation()
        }
    }

    override fun onBinderDied() {
        Timber.d("onBinderDied()")
        shadowSockConnection.disconnect(this)
        shadowSockConnection.connect(this, this)
    }

    private fun changeVpnState(state: BaseService.State) {
        when (state) {
            BaseService.State.Idle -> currentVpnBean.setVpnState(VpnBean.VpnState.IDLE)
            BaseService.State.Connecting -> currentVpnBean.setVpnState(VpnBean.VpnState.CONNECTING)
            BaseService.State.Connected -> currentVpnBean.setVpnState(VpnBean.VpnState.CONNECTED)
            BaseService.State.Stopping -> currentVpnBean.setVpnState(VpnBean.VpnState.STOPPING)
            BaseService.State.Stopped -> currentVpnBean.setVpnState(VpnBean.VpnState.STOPPED)
        }
    }

    private fun vpnConnectedOrStopped(state: BaseService.State) {
        when (state) {
            BaseService.State.Idle -> {}
            BaseService.State.Connecting -> {}
            BaseService.State.Connected -> {
                cancelAnimation()
                gotoConnectResultActivity(true)
                //消除跳转突兀现象
                lifecycleScope.launch {
                    delay(200)
                    connectStateIvBgShow()
                }
            }
            BaseService.State.Stopping -> {}
            BaseService.State.Stopped -> {
                cancelAnimation()
                gotoConnectResultActivity(false)
                connectStateIvBgDismiss()
            }
        }
    }

    private fun gotoConnectResultActivity(isConnected: Boolean) {
        val countryAndCity = if (isConnected) {
            SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
        } else {
            if (isServerPageReqStopVpn) {//VPN选择页面停止VPN
                SPUtil.getString(ConstantUtil.LAST_SELECT_COUNTRY)
            } else {//主页停止VPN
                SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
            }
        }
        val intent = Intent(this@MainActivity, ConnectResultActivity::class.java)
        intent.putExtra(
            ConstantUtil.COUNTRY_AND_CITY_KEY, countryAndCity
        )
        resultLauncher?.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        shadowSockConnection.disconnect(this)
    }

    private fun selectSmartService() {
        val smartListSize = RetrofitUtil.smartServiceList.size
        if (smartListSize == 0) {//如果服务器没有配置smart列表，随机选择一个
            Timber.d("selectSmartService()---服务器没有配置smart列表，排序选出前三再随机选择一个")
            RetrofitUtil.serviceList?.let { RetrofitUtil.smartServiceList.addAll(it) }
            if (RetrofitUtil.smartServiceList.size > 0)//去掉第一个smart服务器占位对象
                RetrofitUtil.smartServiceList.removeAt(0)
            try {
                sortIpDelayTime()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("selectSmartService()---e:$e")
                RetrofitUtil.serviceList?.let { randomSelectVpnAndUpdateInfo(it) }
            }
        } else if (smartListSize <= 3) {//如果smart服务器数量小于等于3,随机选择一个连接
            Timber.d("selectSmartService()---smart服务器数量<=3,随机选择一个连接")
            randomSelectVpnAndUpdateInfo(RetrofitUtil.smartServiceList)
        } else {
            Timber.d("selectSmartService()---smart服务器数量大于3,延时从小到大排序，选出三个最快的，随机选择一个连接")
            try {
                sortIpDelayTime()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("selectSmartService()---e:$e")
                RetrofitUtil.serviceList?.let { randomSelectVpnAndUpdateInfo(it) }
            }
        }
    }

    private fun sortIpDelayTime() {
        var count = 0
        //ip测速
        for (i in 0 until RetrofitUtil.smartServiceList.size) {
            val vpnBean = RetrofitUtil.smartServiceList[i]
            val ip = vpnBean.ip
            ip?.let {
                lifecycleScope.launch {
                    ProjectUtil.delayTest(vpnBean, object : IPDelayTimeCallBack {
                        override fun onIpDelayTime(vpnBean: VpnBean, ipDelayTime: Int) {
                            Timber.d("sortIpDelayTime()---ip:${vpnBean.ip}---ip测速延时:$ipDelayTime")
                            vpnBean.ipDelayTime = ipDelayTime
                            count++
                            if (count == RetrofitUtil.smartServiceList.size) {
                                RetrofitUtil.smartServiceList.sortBy {
                                    it.ipDelayTime
                                }
                                val size = RetrofitUtil.smartServiceList.size
                                if (size > 3) {
                                    val randomVpnList: ArrayList<VpnBean> = ArrayList()
                                    for (j in 0 until 3) {
                                        randomVpnList.add(RetrofitUtil.smartServiceList[j])
                                    }
                                    randomSelectVpnAndUpdateInfo(randomVpnList)
                                } else {
                                    randomSelectVpnAndUpdateInfo(RetrofitUtil.smartServiceList)
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    private fun randomSelectVpnAndUpdateInfo(list: ArrayList<VpnBean>) {
        runOnUiThread {
            if (list.size > 0) {
                val index = Random().nextInt(list.size)
                val selectVpnBean = list[index]
                //构建smart对象
                val smartVpnBean = getSmartBean(selectVpnBean)
                DvViewModel.remainLastVpnBeanInfo()
                DvViewModel.updateVpnBean(smartVpnBean)
                DvViewModel.updateProfile(selectVpnBean)
                Timber.d("randomSelectVpnAndUpdateInfo()---smartVpnBean:${smartVpnBean.ip}")
            } else {
                Timber.d("randomSelectVpnAndUpdateInfo()---list集合无数据")
            }
        }
    }

    private fun getSmartBean(reallyVpnBean: VpnBean): VpnBean {
        val vpnBean = VpnBean()
        vpnBean.country = ConstantUtil.DEFAULT_SERVICE
        vpnBean.ip = reallyVpnBean.ip
        vpnBean.account = reallyVpnBean.account
        vpnBean.pwd = reallyVpnBean.pwd
        vpnBean.port = reallyVpnBean.port
        vpnBean.city = reallyVpnBean.city
        vpnBean.ip = reallyVpnBean.ip
        vpnBean.connectTime.value = ConstantUtil.CONNECT_DEFAULT_TIME
        return vpnBean
    }
}