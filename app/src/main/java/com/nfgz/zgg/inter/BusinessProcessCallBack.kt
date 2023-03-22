package com.nfgz.zgg.inter

interface BusinessProcessCallBack<T> {
    fun onBusinessProcess(t: T)
}