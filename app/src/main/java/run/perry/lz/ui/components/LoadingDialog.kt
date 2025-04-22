package run.perry.lz.ui.components

import android.app.Activity
import run.perry.lz.R
import run.perry.lz.base.BaseDialog
import run.perry.lz.databinding.DialogLoadingBinding

class LoadingDialog(activity: Activity) : BaseDialog<DialogLoadingBinding>(
    { DialogLoadingBinding.inflate(it) }, activity, R.style.LoadingDialog
) {
    override fun main() {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun show() {
        show(null)
    }

    fun show(msg: String? = null) {
        if (!isShowing) {  // 避免重复调用
            super.show()
            if (!msg.isNullOrBlank()) binding.tv.text = msg
        }
    }
}