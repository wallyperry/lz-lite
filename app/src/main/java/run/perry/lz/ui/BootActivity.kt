package run.perry.lz.ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
import run.perry.lz.utils.openInBrowser

class BootActivity : BaseActivity<ActivityBootBinding>({ ActivityBootBinding.inflate(it) }) {

    private val mViewModel by viewModel<BootViewModel>()

    private val mSplashView by lazy {
        SplashView(
            this,
            ImmersionBar.getStatusBarHeight(this),
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

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.loadUiIntentFlow.collect {
                    when (it) {
                        is LoadUiIntent.Error -> {}
                        is LoadUiIntent.Loading -> {
                            if (it.show) loadingDialog.show(it.text) else loadingDialog.dismiss()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.uiStateFlow.map { it.configUiState }.collect {
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
        }

    }

    private fun initBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .statusBarView(binding.statusBarView)
            .transparentNavigationBar()
            .init()
        binding.navigationView.layoutParams = binding.navigationView.layoutParams.apply {
            width = ImmersionBar.getNavigationBarWidth(this@BootActivity)
            height = ImmersionBar.getNavigationBarHeight(this@BootActivity)
        }
    }

    override fun backPressed() { //屏蔽返回操作
    }

}