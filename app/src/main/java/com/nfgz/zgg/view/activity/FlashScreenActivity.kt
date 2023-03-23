package com.nfgz.zgg.view.activity

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.gameacclerator.lightningoptimizer.R
import com.gameacclerator.lightningoptimizer.databinding.ActivityFlashScreenBinding
import com.nfgz.zgg.util.ConstantUtil
import com.nfgz.zgg.util.ProjectUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

/*闪屏页面*/
class FlashScreenActivity : BaseActivity() {
    private lateinit var scopeJob: Job
    private lateinit var flashViewDataBinding: ActivityFlashScreenBinding
    private var canBack = true
    private var isHotLaunch = false
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_flash_screen
    }

    override fun initProcess() {
        flashViewDataBinding = viewDataBinding as ActivityFlashScreenBinding
        isHotLaunch = intent.getBooleanExtra(ConstantUtil.IS_HOT_LAUNCH, false)
        scopeJob = lifecycleScope.launch {
            flow {
                (0 until 100).forEach() {
                    delay(20)
                    emit(it)
                }
            }.onStart {
                //ip限制检测
                ProjectUtil.restrictIpDetect(null)
                canBack = false
            }.onCompletion {
                canBack = true
                gotoHomePage()
            }.collect() { process ->
                flashViewDataBinding.progressbar.progress = process
            }
        }
    }

    private fun gotoHomePage() {
        if (isHotLaunch) {//热启动关闭闪屏页，恢复到之前页面
            Timber.d("gotoHomePage()--关闭页面显示之前页面")
            finish()
        } else {
            Timber.d("gotoHomePage()--跳转到主页")
            val intent = Intent(this@FlashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (!canBack) {
            return
        }
        super.onBackPressed()
    }
}