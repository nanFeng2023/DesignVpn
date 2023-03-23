package com.nfgz.zgg.util

object ConstantUtil {
    //限制国家
    const val COUNTRY_HK = "HK"// 香港
    const val COUNTRY_CN = "CN"// 大陆
    const val COUNTRY_IR = "IR"//伊朗
    const val COUNTRY_MO = "MO" //澳门

    //ip检测地址 ：https://ipinfo.io/json
    const val IP_HOST = "https://ipinfo.io/"
    const val IP_PATH = "/json"

    //VpnBean
    const val CONNECT_DEFAULT_TIME = "00:00:00"

    //菜单相关配置  -------正式配置相关-----------
    const val PRIVACY_POLICY_URL = "https://sites.google.com/view/lightning-optimizer/home"  //隐私协议
    const val MAIL_ACCOUNT = "serrayvonnevic@gmail.com"  //邮箱账号
    const val GOOGLE_STORE_URL = "https://play.google.com/store/apps/details?id="  //商城地址
    //-------正式配置相关-----------------------

    //lottie
    const val GUIDE_IMAGE_ASSETS_FOlDER = "lottie/guide/images"
    const val GUIDE_ANIMATION_JSON = "lottie/guide/images/nfgz_guide_animation.json"

    //key
    const val IS_HOT_LAUNCH = "isHotLaunch"

    //service key
    const val NFGZPD = "nfgzpd"
    const val NFGZACCT = "nfgzacct"
    const val NFGZPT = "nfgzpt"
    const val NFGZCOY = "nfgzcoy"
    const val NFGZCIY = "nfgzciy"
    const val NFGZIP = "nfgzip"

    //native json name
    const val NFGZ_DATA = "nfgz_data.json"

    const val IS_FIRST_INTO_APP = "isFirstIntoApp"
    const val CUR_SELECT_COUNTRY = "curSelectCountry"
    const val DEFAULT_SERVICE = "smart connect"
    const val LAST_SELECT_COUNTRY = "lastSelectCountry"
    const val CUR_SELECT_COUNTRY_LOCATION = "curSelectCountryLocation"

    const val VPN_SWITCH_HINT =
        "If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?"

    const val COUNTRY_AND_CITY_KEY = "country_and_city_key"
}