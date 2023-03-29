package com.gameacclerator.lightningoptimizer.util

object ConstantUtil {
    //ip检测地址 ：https://ipinfo.io/json
    const val IP_HOST = "https://ipinfo.io/"
    const val IP_PATH = "/json"

    //VpnBean
    const val CONNECT_DEFAULT_TIME = "00:00:00"

    //lottie
    const val GUIDE_IMAGE_ASSETS_FOlDER = "lottie/guide/images"
    const val GUIDE_ANIMATION_JSON = "lottie/guide/images/nfgz_guide_animation.json"

    //key
    const val IS_HOT_LAUNCH = "isHotLaunch"

    //native json name
    const val NFGZ_DATA = "nfgz_data.json"
    const val NFGZ_AD = "nfgz_ad.json"

    const val IS_FIRST_INTO_APP = "isFirstIntoApp"
    const val CUR_SELECT_COUNTRY = "curSelectCountry"
    const val DEFAULT_SERVICE = "smart connect"
    const val LAST_SELECT_COUNTRY = "lastSelectCountry"
    const val CUR_SELECT_COUNTRY_LOCATION = "curSelectCountryLocation"

    const val VPN_SWITCH_HINT =
        "If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?"

    const val COUNTRY_AND_CITY_KEY = "country_and_city_key"

    //广告
    //广告item类型
    const val AD_TYPE_OPEN = "open"
    const val AD_TYPE_INTER = "inter"
    const val AD_TYPE_NATIVE = "native"

    //广告来源
    const val AD_SOURCE_ADMOB = "admob"
    const val AD_SOURCE_H5 = "h5"

    //-------------------正式配置相关---------------------
    //限制国家
    const val COUNTRY_HK = "HK"// 香港
    const val COUNTRY_CN = "CN"// 大陆
    const val COUNTRY_IR = "IR"//伊朗
    const val COUNTRY_MO = "MO" //澳门

    //菜单相关配置
    const val PRIVACY_POLICY_URL = "https://sites.google.com/view/lightning-optimizer/home"  //隐私协议
    const val MAIL_ACCOUNT = "serrayvonnevic@gmail.com"  //邮箱账号
    const val GOOGLE_STORE_URL = "https://play.google.com/store/apps/details?id="  //商城地址

    //vpn service key
    const val NFGZPD = "lightning_pwd"
    const val NFGZACCT = "lightning_account"
    const val NFGZPT = "lightning_port"
    const val NFGZCOY = "lightning_coutry"
    const val NFGZCIY = "lightning_city"
    const val NFGZIP = "lightning_ip"

    //远端配置key
    const val REMOTE_SERVICE_CONFIG_KEY = "lightning_serverslist"
    const val REMOTE_SMART_CONFIG_KEY = "lightning_fast"
    const val REMOTE_AD_CONFIG_KEY = "lightning_ad"

    //广告日显示限制
    const val DAY_SHOW_LIMIT = "day_show_limit"
    const val DAY_CLICK_LIMIT = "day_click_limit"

    //广告位key
    const val AD_SPACE_OPEN_ON = "open_on"
    const val AD_SPACE_INTER_CLICK = "inter_click"
    const val AD_SPACE_INTER_IB = "inter_ib"
    const val AD_SPACE_NATIVE_HOME = "native_home"
    const val AD_SPACE_NATIVE_RESULT = "native_result"

    //广告item key
    const val AD_ITEM_ID = "ad_item_id"
    const val AD_ITEM_SOURCE = "ad_item_source"
    const val AD_ITEM_TYPE = "ad_item_type"
    const val AD_ITEM_P = "ad_item_p"

    //-------------------正式配置相关---------------------

    //广告过期时间
    const val AD_EXPIRATION_TIME = 4

    //广告展示日上限
    const val AD_SHOW_DAY_LIMIT_KEY = "ad_show_day_limit_key"

    //广告点击日上限
    const val AD_CLICK_DAY_LIMIT_KEY = "ad_click_day_limit_key"

    const val LAST_AD_RECORD_TIME = "lastAdRecordTime"
}