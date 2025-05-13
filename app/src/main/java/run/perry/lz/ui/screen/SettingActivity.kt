package run.perry.lz.ui.screen

import com.gyf.immersionbar.ktx.immersionBar
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity<ActivitySettingBinding>({ ActivitySettingBinding.inflate(it) }) {
    override fun main() {
        initBar()
    }

    private fun initBar() {
        immersionBar {
            statusBarView(binding.statusBarView)
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
        binding.ibBack.setOnClickListener { backPressed() }
    }
}