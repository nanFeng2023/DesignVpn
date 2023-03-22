package com.nfgz.zgg.bean

import androidx.lifecycle.MutableLiveData

class VpnBean() {
    //基础字段
    var account: String? = null
    var country: String? = null
    var pwd: String? = null
    var port: Int = 0
    var city: String? = null
    var ip: String? = null
    var ipDelayTime: Int? = 0

    var countryResId = MutableLiveData<Int>()
    var connectTime = MutableLiveData<String>()
    var title = MutableLiveData<String>()
    var state = VpnState.IDLE

    //连接状态
    var idle: Boolean = true
    var connecting: Boolean = false
    var connected: Boolean = false
    var stopping: Boolean = false
    var stopped: Boolean = false

    //guide显示状态
    var isShowingGuide = false

    //是否是限制国家
    var isBelongRestrictCountry = false

    enum class VpnState(val canStop: Boolean = false) {
        IDLE,
        CONNECTING(true),
        CONNECTED(true),
        STOPPING,
        STOPPED
    }

    fun setVpnState(state: VpnState) {
        this.state = state
        when (state) {
            VpnState.IDLE -> {
                idle = true
                connecting = false
                connected = false
                stopping = false
                stopped = false
            }
            VpnState.CONNECTING -> {
                idle = false
                connecting = true
                connected = false
                stopping = false
                stopped = false

            }
            VpnState.CONNECTED -> {
                idle = false
                connecting = false
                connected = true
                stopping = false
                stopped = false

            }
            VpnState.STOPPING -> {
                idle = false
                connecting = false
                connected = false
                stopping = true
                stopped = false

            }
            VpnState.STOPPED -> {
                idle = false
                connecting = false
                connected = false
                stopping = false
                stopped = true
            }
        }
    }

    fun getVpnShowTitle(): String {
        var title = ""
        if (!country.isNullOrEmpty()) {
            title = country as String
            if (!city.isNullOrEmpty()) {
                title = "$country-$city"
            }
        }
        return title
    }

}