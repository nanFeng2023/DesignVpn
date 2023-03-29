package com.nfgz.zgg

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import com.github.shadowsocks.Core
import com.google.android.gms.ads.MobileAds
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.nfgz.zgg.net.ReqApiIml
import com.nfgz.zgg.net.retrofit.RetrofitUtil
import com.nfgz.zgg.view.activity.MainActivity

class App : Application() {
    companion object {
        lateinit var appContext: Application
        lateinit var activityLifecycleCallBack: ActivityLifecycleCallBack
    }

    init {
        appContext = this
    }

    override fun onCreate() {
        super.onCreate()
        Core.init(this, MainActivity::class)
        if (isMainProcess()) {
            Firebase.initialize(this)
            activityLifecycleCallBack = ActivityLifecycleCallBack()
            registerActivityLifecycleCallbacks(activityLifecycleCallBack)
            //广告注册
            MobileAds.initialize(this) {}
            if (BuildConfig.DEBUG) {
//                Timber.plant(Timber.DebugTree())
            }
            if (!BuildConfig.DEBUG) {
                //预加载远端服务器数据
                RetrofitUtil.loadRemoteConfigureData()
            }
            //针对google系统后台杀掉进程，而子进程VPN仍然连接的问题，解决办法：冷启动关闭vpn连接
            Core.stopService()
            //服务器数据预加载
            ReqApiIml().getVpnServiceList()
            //smart服务器数据预加载
            ReqApiIml().getVpnSmartList()
            //广告数据预加载
            ReqApiIml().getAdDataList()
        }

    }

    private fun isMainProcess(): Boolean {
        return appContext.packageName.equals(getCurProcessName())
    }

    @SuppressLint("ServiceCast")
    private fun getCurProcessName(): String? {
        val myPid = android.os.Process.myPid()
        val activityManager: ActivityManager =
            appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        for (runningAppProcess in runningAppProcesses) {
            if (runningAppProcess.pid == myPid) {
                return runningAppProcess.processName
            }
        }
        return null
    }

}