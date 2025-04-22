package run.perry.lz.ui.components

import android.app.Activity
import android.view.Gravity
import android.view.View
import run.perry.lz.R
import run.perry.lz.base.BaseDialog
import run.perry.lz.databinding.DialogTipsBinding

class TipsDialog(activity: Activity) : BaseDialog<DialogTipsBinding>(
    { DialogTipsBinding.inflate(it) }, activity, R.style.dialog_tips
) {

    private var positive: ((TipsDialog) -> Unit)? = null
    private var negative: ((TipsDialog) -> Unit)? = null
    private var positiveDismiss: Boolean = true
    private var negativeDismiss: Boolean = true

    override fun main() {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        binding.tvTitle.text = "提示"
        binding.tvPositive.text = "确认"
        binding.tvNegative.text = "取消"
        binding.tvPositive.setOnClickListener {
            if (positiveDismiss) dismiss()
            positive?.let { it(this@TipsDialog) }
        }
        binding.tvNegative.setOnClickListener {
            if (negativeDismiss) dismiss()
            negative?.let { it(this@TipsDialog) }
        }
    }

    fun show(func: TipsDialog.() -> Unit) = apply {
        this.show()
        this.func()
    }

    fun title(text: String?) = apply { binding.tvTitle.text = text?.ifBlank { "提示" } }
    fun content(text: String?, gravity: Int = Gravity.CENTER) = apply {
        binding.tvContent.text = text.orEmpty()
        binding.tvContent.gravity = gravity
    }

    fun hideNegative() = apply {
        binding.tvNegative.visibility = View.GONE
        negative = null
    }

    fun hidePositive() = apply {
        binding.tvPositive.visibility = View.GONE
        positive = null
    }

    fun positive(
        text: String = "确认",
        dismiss: Boolean = true,
        click: ((TipsDialog) -> Unit)? = null,
    ) = apply {
        binding.tvPositive.text = text
        positiveDismiss = dismiss
        positive = click
    }

    fun negative(
        text: String = "取消",
        dismiss: Boolean = true,
        click: ((TipsDialog) -> Unit)? = null,
    ) = apply {
        binding.tvNegative.text = text
        negativeDismiss = dismiss
        negative = click
    }
}