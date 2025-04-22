package run.perry.lz.base

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import run.perry.lz.R
import java.lang.ref.WeakReference

abstract class BaseDialog<VB : ViewBinding>(
    val block: (LayoutInflater) -> VB,
    activity: Activity,
    themeId: Int = R.style.dialog_base
) : Dialog(activity, themeId) {

    private val activityRef = WeakReference(activity)
    protected lateinit var binding: VB
    protected abstract fun main()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = activityRef.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            dismiss()
            return
        }

        binding = block(activity.layoutInflater)
        setContentView(binding.root)
    }

    override fun show() {
        val activity = activityRef.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        if (!::binding.isInitialized) {
            binding = block(activity.layoutInflater)
            setContentView(binding.root)
        }

        super.show()
        main()
    }

}