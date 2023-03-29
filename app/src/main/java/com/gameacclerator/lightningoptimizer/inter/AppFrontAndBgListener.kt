package com.gameacclerator.lightningoptimizer.inter

import android.app.Activity

/*前后台监听回调*/
interface AppFrontAndBgListener {
    fun onAppToFront(activity: Activity)
    fun onAppToBackGround(activity: Activity)
}