package run.perry.lz.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import run.perry.lz.ui.components.LoadingDialog
import run.perry.lz.utils.ActivityManager

abstract class BaseActivity<VB : ViewBinding>(val block: (LayoutInflater) -> VB) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    protected abstract fun main()
    protected val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = block(layoutInflater)
        ActivityManager.addActivity(this)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
        setContentView(binding.root)

        main()

    }

    protected open fun backPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        ActivityManager.removeActivity(this)
    }
}