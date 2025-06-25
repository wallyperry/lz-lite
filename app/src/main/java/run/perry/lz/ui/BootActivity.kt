package run.perry.lz.ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.navigationBarHeight
import com.gyf.immersionbar.ktx.navigationBarWidth
import com.gyf.immersionbar.ktx.statusBarHeight
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import run.perry.lz.BuildConfig
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.base.LoadUiIntent
import run.perry.lz.databinding.ActivityBootBinding
import run.perry.lz.domain.http.utils.extractPath
import run.perry.lz.ui.components.SplashView
import run.perry.lz.ui.intent.BootIntent
import run.perry.lz.ui.screen.MainActivity
import run.perry.lz.ui.state.ConfigUiState
import run.perry.lz.ui.vm.BootViewModel
import run.perry.lz.utils.collectLatestOnLifecycle
import run.perry.lz.utils.openInBrowser

class BootActivity : BaseActivity<ActivityBootBinding>({ ActivityBootBinding.inflate(it) }) {

    private val mViewModel by viewModel<BootViewModel>()

    private val mSplashView by lazy {
        SplashView(
            this,
            statusBarHeight,
            3,
            R.mipmap.pic_splash,
            imageClickCallback = { openInBrowser(it) },
            dismissCallback = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            })
    }

    override fun main() {

        initBar()

        mViewModel.loadUiIntentFlow.collectLatestOnLifecycle(this) {
            when (it) {
                is LoadUiIntent.Error -> {}
                is LoadUiIntent.Loading -> {
                    if (it.show) loadingDialog.show(it.text) else loadingDialog.dismiss()
                }
            }
        }

        mViewModel.uiStateFlow.map { it.configUiState }.collectLatestOnLifecycle(this) {
            when (it) {
                is ConfigUiState.INIT -> {
                    mSplashView.show()
                    mViewModel.sendUiIntent(BootIntent.GetConfig(BuildConfig.GATEWAY_ADDRESS.extractPath()))
                }

                is ConfigUiState.SUCCESS -> {
                    mSplashView.updateData(
                        it.bean?.splash?.img, it.bean?.splash?.url
                    )
                }

                is ConfigUiState.DONE -> mSplashView.startCountDown()
            }
        }

    }

    private fun initBar() {
        immersionBar {
            statusBarDarkFont(true)
            statusBarView(binding.statusBarView)
            transparentNavigationBar()
        }
        binding.navigationView.layoutParams = binding.navigationView.layoutParams.apply {
            width = navigationBarWidth
            height = navigationBarHeight
        }
    }

    override fun backPressed() { //屏蔽返回操作
    }

}