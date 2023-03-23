package com.nfgz.zgg.viewmodel

import androidx.lifecycle.ViewModel
import com.github.shadowsocks.Core
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.ProjectUtil
import com.nfgz.zgg.util.SPUtil
import com.nfgz.zgg.util.TimeUtil
import timber.log.Timber

object DvViewModel : ViewModel() {
    var resetVpnBean = VpnBean()
    val currentVpnBean: VpnBean = VpnBean()

    fun initVpnState() {
        val countryAndCity = SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
        val country = ProjectUtil.splitStrGetCountry(countryAndCity)
        currentVpnBean.country = country
        if (countryAndCity.isNullOrBlank()) {
            currentVpnBean.title.value = ConstantUtil.DEFAULT_SERVICE
        } else {
            currentVpnBean.title.value = countryAndCity
        }
        currentVpnBean.countryResId.value = ProjectUtil.selectCountryIcon(country)
        currentVpnBean.connectTime.value = TimeUtil.curConnectTime

        val profile = ProfileManager.getProfile(DataStore.profileId)
            ?: ProfileManager.createProfile(Profile())
        profile.id = DataStore.profileId
        Core.switchProfile(profile.id)
    }

    fun updateVpnBean(bean: VpnBean) {
        bean.country.let { country ->
            currentVpnBean.country = country
            currentVpnBean.countryResId.value = ProjectUtil.selectCountryIcon(country)
            if (country == ConstantUtil.DEFAULT_SERVICE) {
                currentVpnBean.title.value = country
            } else {
                currentVpnBean.title.value = bean.getVpnShowTitle()
            }
        }
        currentVpnBean.account = bean.account
        currentVpnBean.pwd = bean.pwd
        currentVpnBean.port = bean.port
        currentVpnBean.city = bean.city
        currentVpnBean.ip = bean.ip
        currentVpnBean.connectTime.value = TimeUtil.curConnectTime
        Timber.d("updateVpnBean()---vpnBean更新完成")
    }

    fun updateProfile(vpnBean: VpnBean) {
        val profile = ProfileManager.getProfile(DataStore.profileId)
            ?: ProfileManager.createProfile(Profile())
        profile.host = vpnBean.ip.toString()
        profile.name = vpnBean.getVpnShowTitle()
        profile.method = vpnBean.account.toString()
        profile.password = vpnBean.pwd.toString()
        profile.remotePort = vpnBean.port
        ProfileManager.updateProfile(profile)
        Timber.d("updateProfile()---profile更新完成")
    }

    fun remainLastVpnBeanInfo() {
        resetVpnBean.country = currentVpnBean.country
        resetVpnBean.title = currentVpnBean.title
        resetVpnBean.countryResId.value = currentVpnBean.countryResId.value
        resetVpnBean.account = currentVpnBean.account
        resetVpnBean.pwd = currentVpnBean.pwd
        resetVpnBean.port = currentVpnBean.port
        resetVpnBean.city = currentVpnBean.city
        resetVpnBean.ip = currentVpnBean.ip
        Timber.d("remainLastVpnBeanInfo()")
    }
}