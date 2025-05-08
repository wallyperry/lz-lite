package run.perry.lz.ui.screen

import coil.load
import com.gyf.immersionbar.ktx.immersionBar
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.data.AppStore
import run.perry.lz.databinding.ActivityShareBinding
import run.perry.lz.utils.copyToClipboard
import java.lang.String

class ShareActivity : BaseActivity<ActivityShareBinding>({ ActivityShareBinding.inflate(it) }) {
    override fun main() {
        initBar()

        binding.ivQr.load(AppStore.shareQr)
        binding.tvScanTip.text = String.format("扫码下载%sAPP", getString(R.string.app_name))
        binding.slCopy.setOnClickListener { AppStore.shareUrl.copyToClipboard() }
    }

    private fun initBar() {
        immersionBar {
            statusBarView(binding.statusBarView)
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
        binding.ibBack.setOnClickListener { finish() }
    }
}