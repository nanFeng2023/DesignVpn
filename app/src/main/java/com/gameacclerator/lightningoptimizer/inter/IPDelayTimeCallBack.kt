package com.gameacclerator.lightningoptimizer.inter

import com.gameacclerator.lightningoptimizer.bean.VpnBean

interface IPDelayTimeCallBack {
    fun onIpDelayTime(vpnBean: VpnBean, ipDelayTime: Int)
}