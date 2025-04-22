package run.perry.lz.utils

import android.view.Gravity
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import run.perry.lz.R

fun String.toastSuccess() {
    Toaster.show(ToastParams().apply {
        text = this@toastSuccess
        style = CustomToastStyle(R.layout.toast_success, Gravity.BOTTOM, 0, 100.dp)
    })
}

fun String.toastError() {
    Toaster.show(ToastParams().apply {
        text = this@toastError
        style = CustomToastStyle(R.layout.toast_error, Gravity.BOTTOM, 0, 100.dp)
    })
}

fun String.toastWarning() {
    Toaster.show(ToastParams().apply {
        text = this@toastWarning
        style = CustomToastStyle(R.layout.toast_warning, Gravity.BOTTOM, 0, 100.dp)
    })
}

fun String.toastInfo() {
    Toaster.show(ToastParams().apply {
        text = this@toastInfo
        style = CustomToastStyle(R.layout.toast_info, Gravity.BOTTOM, 0, 100.dp)
    })
}