package com.gameacclerator.lightningoptimizer.net

interface ReqCallBack<T> {
    fun onSuccess(response: T?)
    fun onFail(t: Throwable)
}