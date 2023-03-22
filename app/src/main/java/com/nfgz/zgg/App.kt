package com.nfgz.zgg

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import com.github.shadowsocks.Core
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.net.ReqApiIml
import com.nfgz.zgg.view.activity.MainActivity
import com.nfgz.zgg.viewmodel.DvViewModel
import timber.log.Timber

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
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
            //针对google系统后台杀掉进程，而子进程VPN仍然连接的问题，解决办法：冷启动关闭vpn连接
            Core.stopService()
            //服务器数据预加载
            ReqApiIml().getVpnServiceList()
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