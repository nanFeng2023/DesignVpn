package com.nfgz.zgg.net

interface ReqCallBack<T> {
    fun onSuccess(response: T?)
    fun onFail(t: Throwable)
}