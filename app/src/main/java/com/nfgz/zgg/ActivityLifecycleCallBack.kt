package com.nfgz.zgg

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.os.Bundle
import com.nfgz.zgg.inter.AppFrontAndBgListener
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.view.activity.FlashScreenActivity
import timber.log.Timber

class ActivityLifecycleCallBack : ActivityLifecycleCallbacks {
    private var lastExitTime = -1L
    private var finalCount: Int = 0
    var appFrontAndBgListener: AppFrontAndBgListener? = null
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        finalCount++
        //如果finalCount==1,说明应用是后台到前台
        if (finalCount == 1) {
            val currentTimeMillis = System.currentTimeMillis()
            val intervalTime = currentTimeMillis - lastExitTime
            Timber.d("onActivityStarted()---activity:${activity.localClassName}---intervalTime:$intervalTime")
            appFrontAndBgListener?.onAppToFront()
            if (lastExitTime != -1L && intervalTime > 3000) {
                Timber.d("onActivityStarted()---热启动")
                val intent = Intent(activity, FlashScreenActivity::class.java)
                intent.putExtra(ConstantUtil.IS_HOT_LAUNCH, true)
                activity.startActivity(intent)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        finalCount--
        //finalCount==0,说明应用前台到后台
        if (finalCount == 0) {
            Timber.d("onActivityStopped()---应用退到后台---activity:${activity.localClassName}")
            lastExitTime = System.currentTimeMillis()
            appFrontAndBgListener?.onAppToBackGround()
        }

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}