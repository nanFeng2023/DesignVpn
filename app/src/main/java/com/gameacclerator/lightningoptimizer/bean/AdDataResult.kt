package com.gameacclerator.lightningoptimizer.bean

class AdDataResult {
    var dayShowLimit: Int = 0 //展示上限
    var dayClickLimit: Int = 0//点击上限
    val adHashMap = HashMap<String, ArrayList<AdBean>>()
}