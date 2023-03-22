package com.nfgz.zgg.inter

import com.nfgz.zgg.bean.VpnBean

interface IPDelayTimeCallBack {
    fun onIpDelayTime(vpnBean: VpnBean, ipDelayTime: Int)
}