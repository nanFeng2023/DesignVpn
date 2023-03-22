package com.nfgz.zgg

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityManager {
    private var currentActivityWeakRef: WeakReference<Activity>? = null
    fun getCurrentActivity(): Activity? {
        return currentActivityWeakRef?.get()
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivityWeakRef = WeakReference<Activity>(activity)
    }
}