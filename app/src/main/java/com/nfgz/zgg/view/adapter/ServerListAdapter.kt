package com.nfgz.zgg.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gameacclerator.lightningoptimizer.R
import com.nfgz.zgg.bean.VpnBean
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.ProjectUtil
import com.nfgz.zgg.util.SPUtil
import com.nfgz.zgg.viewmodel.DvViewModel

class ServerListAdapter(private val serviceList: ArrayList<VpnBean>) :
    RecyclerView.Adapter<ViewHolder>() {
    private val curSelectCountry = SPUtil.getString(ConstantUtil.CUR_SELECT_COUNTRY)
    lateinit var onItemClick: (position: Int) -> Unit

    companion object {
        private const val ORDINARY_TYPE: Int = 1
        private const val SPECIAL_TYPE: Int = 2
    }

    inner class OrdinaryViewHolder(view: View) : ViewHolder(view) {
        val ivCountry: ImageView = view.findViewById(R.id.iv_country)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
    }

    inner class SpecialViewHolder(view: View) : ViewHolder(view) {
        val ivCountry: ImageView = view.findViewById(R.id.iv_country)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vpnBean = serviceList[position]
        if (holder is OrdinaryViewHolder) {
            holder.apply {
                tvTitle.text = vpnBean.getVpnShowTitle()
                vpnBean.country.let { country ->
                    ivCountry.setImageResource(ProjectUtil.selectCountryIcon(country))
                }
            }
        } else if (holder is SpecialViewHolder) {
            holder.apply {
                tvTitle.text = vpnBean.getVpnShowTitle()
                vpnBean.country.let { country ->
                    ivCountry.setImageResource(ProjectUtil.selectCountryIcon(country))
                }
            }
        }

        holder.itemView.setOnClickListener {
            if ((position == 0 && DvViewModel.currentVpnBean.state == VpnBean.VpnState.IDLE)
                || curSelectCountry != vpnBean.getVpnShowTitle()
            ) {
                onItemClick.invoke(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == SPECIAL_TYPE) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_special_item, parent, false)
            SpecialViewHolder(view)

        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_item, parent, false)
            OrdinaryViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val viewType: Int
        val vpnBean = serviceList[position]
        viewType = if (curSelectCountry == vpnBean.getVpnShowTitle()) {
            SPECIAL_TYPE
        } else {
            ORDINARY_TYPE
        }
        return viewType
    }

}