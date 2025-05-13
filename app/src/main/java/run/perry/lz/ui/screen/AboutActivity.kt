package run.perry.lz.ui.screen

import android.content.Intent
import com.gyf.immersionbar.ktx.immersionBar
import com.umeng.analytics.MobclickAgent
import run.perry.lz.BuildConfig
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.data.AppStore
import run.perry.lz.databinding.ActivityAboutBinding
import run.perry.lz.ui.components.TipsDialog
import run.perry.lz.utils.UM_EVENT_AUTHOR
import run.perry.lz.utils.asString
import run.perry.lz.utils.checkVersionUpdate
import run.perry.lz.utils.copyToClipboard
import java.util.Calendar

class AboutActivity : BaseActivity<ActivityAboutBinding>({ ActivityAboutBinding.inflate(it) }) {
    override fun main() {
        initBar()

        binding.run {
            ivLogo.setImageResource(R.mipmap.ic_launcher)

            tvVersion.text = "当前版本：%s".format(BuildConfig.VERSION_NAME)
            tvBuildTime.text = "编译时间：%s".format(R.string.build_time.asString)
            tvCopyright.text = "© %s · %s".format(Calendar.getInstance().get(Calendar.YEAR), R.string.app_name.asString)

            tvCheckUpdate.setOnClickListener { checkVersionUpdate(true) }
            tvAuthor.setOnClickListener {
                if (AppStore.contactContent.isEmpty()) {
                    "抱歉，作者联系方式暂未公开！"
                    return@setOnClickListener
                }

                TipsDialog(this@AboutActivity).show {
                    title(AppStore.contactTitle)
                    content(AppStore.contactContent)
                    positive("复制") {
                        AppStore.contactContent.copyToClipboard()
                        MobclickAgent.onEventObject(
                            this@AboutActivity, UM_EVENT_AUTHOR,
                            mapOf(
                                "title" to AppStore.contactTitle,
                                "content" to AppStore.contactContent
                            )
                        )
                    }
                }
            }
            tvShare.setOnClickListener { startActivity(Intent(this@AboutActivity, ShareActivity::class.java)) }
        }

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