package run.perry.lz.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.core.context.GlobalContext
import run.perry.lz.databinding.ViewRvStateBinding
import java.util.Locale
import java.util.concurrent.TimeUnit

fun appContext(): Context = GlobalContext.get().get()

fun Context.openInBrowser(url: String?) {
    try {
        if (url.isNullOrBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

val displayMetrics: DisplayMetrics get() = appContext().resources.displayMetrics

val Int.dp: Int get() = (this * displayMetrics.density + 0.5f).toInt()

val Int.asString: String get() = appContext().resources.getString(this)
val Int.asColor: Int get() = ContextCompat.getColor(appContext(), this)
val Int.asDimenPx: Int get() = appContext().resources.getDimensionPixelSize(this)

fun View.setParams(w: Int? = null, h: Int? = null): ViewGroup.LayoutParams? = layoutParams.apply {
    w?.let { width = it }
    h?.let { height = it }
}

fun View.setMargin(l: Int, t: Int, r: Int, b: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        (layoutParams as ViewGroup.MarginLayoutParams).setMargins(l, t, r, b)
        requestLayout()
    }
}

@SuppressLint("RestrictedApi", "VisibleForTests")
fun Dialog?.disableShapeAnimation() {
    try {
        val bottomSheetDialog = this as BottomSheetDialog
        bottomSheetDialog.behavior.disableShapeAnimations()
    } catch (ex: Exception) {
        Log.e("BaseBottomSheet", "disableShapeAnimation Exception:", ex)
    }
}

fun Dialog?.applyFullHeightDialog(activity: Activity) {
    val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    val height = windowMetrics.bounds.height()

    this?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { bs ->
        BottomSheetBehavior.from(bs).peekHeight = height
    }
}

fun View.showDynamicPopup(items: List<Pair<String, () -> Unit>>) {
    PopupMenu(context, this).apply {
        items.forEachIndexed { index, (title, action) ->
            menu.add(0, index, index, title)
        }
        setOnMenuItemClickListener { item ->
            items[item.itemId].second.invoke()
            true
        }
        show()
    }
}

fun Activity.inflateStateView(
    @RawRes
    lottieRaw: Int, msg: String = "", click: () -> Unit = {}
): View = ViewRvStateBinding.inflate(LayoutInflater.from(this)).apply {
    lav.setAnimation(lottieRaw)
    if (msg.isBlank()) {
        tvMsg.visibility = View.INVISIBLE
    } else {
        tvMsg.visibility = View.VISIBLE
        tvMsg.text = msg
        tvMsg.setOnClickListener { click.invoke() }
    }
}.root

@SuppressLint("DefaultLocale")
fun Long.toFormattedDuration(isAlbum: Boolean, isSeekBar: Boolean) = try {
    val defaultFormat = if (isAlbum) "%02dm:%02ds" else "%02d:%02d"
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this)
    if (minutes < 60) {
        String.format(
            Locale.getDefault(), defaultFormat,
            minutes,
            seconds - TimeUnit.MINUTES.toSeconds(minutes)
        )
    } else {
        // https://stackoverflow.com/a/9027379
        when {
            isSeekBar -> String.format(
                "%02d:%02d:%02d",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours),
                seconds - TimeUnit.MINUTES.toSeconds(minutes)
            )

            else -> String.format(
                "%02dh:%02dm",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours)
            )
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
    ""
}

fun String?.copyToClipboard() {
    if (isNullOrEmpty()) {
        "复制失败".toastError()
        return
    }
    try {
        val cm = appContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(this, this))
        "已复制".toastSuccess()
    } catch (_: Exception) {
        "复制失败".toastError()
    }
}