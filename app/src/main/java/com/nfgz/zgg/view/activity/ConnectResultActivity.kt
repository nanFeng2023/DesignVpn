package com.nfgz.zgg.view.activity

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.databinding.ActivityConnectResultBinding
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.ProjectUtil
import com.nfgz.zgg.util.TimeUtil
import com.nfgz.zgg.viewmodel.DvViewModel

class ConnectResultActivity : BaseActivity() {
    private lateinit var connectResultDataBinding: ActivityConnectResultBinding
    override fun getLayoutId(): Int {
        return R.layout.activity_connect_result
    }

    override fun initProcess() {
        connectResultDataBinding = viewDataBinding as ActivityConnectResultBinding
        connectResultDataBinding.lifecycleOwner = this
        connectResultDataBinding.viewModel = DvViewModel

        if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
            connectResultDataBinding.clTitleBar.tvTitle.text = getString(R.string.connect_success)
            connectResultDataBinding.ivIcon.setImageResource(R.mipmap.ic_nfgz_vpn_connect_success)
            connectResultDataBinding.tvVpnConnectState.text =
                getString(R.string.connected_successfully)
            TimeUtil.startAccumulateTime()
        } else if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.STOPPED) {
            connectResultDataBinding.clTitleBar.tvTitle.text = getString(R.string.disconnected)
            connectResultDataBinding.ivIcon.setImageResource(R.mipmap.ic_nfgz_app_icon)
            connectResultDataBinding.tvVpnConnectState.text = getString(R.string.vpn_disconnected)
            TimeUtil.pauseTime()
        }

        connectResultDataBinding.clTitleBar.ivBack.clickDelay {
            finish()
        }

        val activityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val bundle = it.data?.extras
                    val intent = Intent()
                    bundle?.let { bd -> intent.putExtras(bd) }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        //产品上可以不跳转，屏蔽掉
//        connectResultDataBinding.clVpn.clickDelay {
//            activityResult.launch(Intent(this, ServiceListActivity::class.java))
//        }

        val countryAndCity = intent.getStringExtra(ConstantUtil.COUNTRY_AND_CITY_KEY)
        val country = ProjectUtil.splitStrGetCountry(countryAndCity)
        country.let {
            connectResultDataBinding.ivSmallAppIcon.setImageResource(
                ProjectUtil.selectCountryIcon(it)
            )
            connectResultDataBinding.tvShowVpn.text = countryAndCity
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.STOPPED) {
            TimeUtil.resetTime()
        }
    }

}