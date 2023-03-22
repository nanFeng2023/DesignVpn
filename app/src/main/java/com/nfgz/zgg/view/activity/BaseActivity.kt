package com.nfgz.zgg.view.activity

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.nfgz.zgg.util.StatusBarUtil

abstract class BaseActivity : AppCompatActivity() {
    var viewDataBinding: ViewDataBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenAdaption()
        StatusBarUtil.immersiveStatusBar(this, Color.WHITE)
        viewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        initProcess()
        setClickEvent()
        //禁止横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //禁止键盘挤压布局
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    abstract fun getLayoutId(): Int
    abstract fun initProcess()
    open fun setClickEvent() {}

    private fun screenAdaption() {
        val metrics: DisplayMetrics = resources.displayMetrics
        val td = metrics.heightPixels / 760f
        val dpi = (160 * td).toInt()
        metrics.density = td
        metrics.scaledDensity = td
        metrics.densityDpi = dpi
    }

    infix fun View.clickDelay(listener: (view: View) -> Unit) {
        val clickEffectiveTime = 500L
        var lastClickTime: Long = 0
        this.setOnClickListener {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - lastClickTime > clickEffectiveTime) {
                lastClickTime = System.currentTimeMillis()
                listener.invoke(this)
            }
        }
    }
}