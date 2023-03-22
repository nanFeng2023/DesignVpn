package com.nfgz.zgg.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nfgz.zgg.R
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.databinding.ActivityServiceListBinding
import com.nfgz.zgg.net.retrofit.RetrofitUtil
import com.nfgz.zgg.util.AlertDialogUtil
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.SPUtil
import com.nfgz.zgg.view.adapter.ServerListAdapter
import com.nfgz.zgg.viewmodel.DvViewModel

class ServiceListActivity : BaseActivity() {
    private lateinit var serviceListViewDataBinding: ActivityServiceListBinding
    override fun getLayoutId(): Int {
        return R.layout.activity_service_list
    }

    override fun initProcess() {
        serviceListViewDataBinding = viewDataBinding as ActivityServiceListBinding
        serviceListViewDataBinding.titleBar.tvTitle.text = getString(R.string.server)
        val serverListAdapter = RetrofitUtil.serviceList?.let { ServerListAdapter(it) }
        serviceListViewDataBinding.rv.layoutManager = LinearLayoutManager(this)
        serviceListViewDataBinding.rv.adapter = serverListAdapter
        if (serverListAdapter != null) {
            serverListAdapter.onItemClick = { position ->
                dialogHint(position)
            }
        }
    }

    override fun setClickEvent() {
        serviceListViewDataBinding.titleBar.ivBack.clickDelay {
            finish()
        }
    }

    private fun dialogHint(position: Int) {
        if (DvViewModel.currentVpnBean.state == VpnBean.VpnState.CONNECTED) {
            AlertDialogUtil().createDialog(
                this@ServiceListActivity, null,
                ConstantUtil.VPN_SWITCH_HINT, "yes",
                { _, _ ->
                    setResultForActivity(position)
                }, "no", null
            )
        } else {
            setResultForActivity(position)
        }
    }

    private fun setResultForActivity(position: Int) {
        //上次选中的国家
        val lastSelectCountry = SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
        SPUtil.putString(ConstantUtil.LAST_SELECT_COUNTRY, lastSelectCountry)
        //保存选中的VPN 格式：国家-城市
        SPUtil.putString(ConstantUtil.CUR_SELECT_COUNTRY, RetrofitUtil.serviceList?.get(position)?.getVpnShowTitle())

        //页面回传值
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt(ConstantUtil.CUR_SELECT_COUNTRY_LOCATION, position)
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()
    }
}