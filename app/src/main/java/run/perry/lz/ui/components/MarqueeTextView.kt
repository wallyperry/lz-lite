package run.perry.lz.ui.components

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setSingleLine()
        setLines(1)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.MARQUEE
    }

    override fun isFocused() = true
}