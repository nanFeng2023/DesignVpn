package com.gameacclerator.lightningoptimizer.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.ceil

object StatusBarUtil {

    /*设置状态栏文字颜色*/
    fun setStatusBarTextColor(activity: Activity, @ColorInt color: Int) {
        val calculateLuminance = ColorUtils.calculateLuminance(color)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let {
            if (color == Color.TRANSPARENT) {
                //如果是透明颜色就默认设置为黑色
                it.isAppearanceLightStatusBars = true
            } else {
                it.isAppearanceLightStatusBars = calculateLuminance >= 0.5
            }
        }
    }

    fun immersiveStatusBar(activity: Activity, @ColorInt color: Int) {
        val window = activity.window.apply {
            statusBarColor = Color.TRANSPARENT
        }
        setStatusBarTextColor(activity, color)
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    // 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    fun dip2px(context: Context, dpValue: Float): Int {
        // 获取当前手机的像素密度（1个dp对应几个px）
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt() // 四舍五入取整
    }

    // 根据手机的分辨率从 px(像素) 的单位 转成为 dp
    fun px2dip(context: Context, pxValue: Float): Int {
        // 获取当前手机的像素密度（1个dp对应几个px）
        val scale: Float = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt() // 四舍五入取整
    }

    fun getStatusBarHeight(context: Context): Double {
        return ceil((25 * context.resources.displayMetrics.density).toDouble())
    }

    fun getStatusBarHeight2(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}