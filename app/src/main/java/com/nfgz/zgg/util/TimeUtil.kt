package com.nfgz.zgg.util

import android.os.Looper
import android.os.Handler
import android.os.SystemClock
import com.nfgz.zgg.viewmodel.DvViewModel

object TimeUtil {
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private var millisecondsRecord = 0L
    private var startTime = 0L
    private var timeBuff = 0L
    var curConnectTime: String? = ConstantUtil.CONNECT_DEFAULT_TIME

    private val runnable = object : Runnable {
        override fun run() {
            millisecondsRecord = System.currentTimeMillis() - startTime
            calculateTime()
            handler?.postDelayed(this, 1000)
        }
    }

    private fun calculateTime() {
        val accumulatedTime = timeBuff + millisecondsRecord
        val seconds = accumulatedTime / 1000 % 60
        val minutes = accumulatedTime / 1000 / 60 % 60
        val hours = accumulatedTime / 1000 / 60 / 24 % 24
        curConnectTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        //更新值到当前到vpnBean
        DvViewModel.currentVpnBean.connectTime.value = curConnectTime
    }

    fun startAccumulateTime() {
        startTime = System.currentTimeMillis()
        handler?.postDelayed(runnable, 1000)
    }

    fun resetTime() {
        millisecondsRecord = 0L
        timeBuff = 0L
        calculateTime()
    }

    fun pauseTime() {
        timeBuff += millisecondsRecord
        handler?.removeCallbacks(runnable)
    }

}