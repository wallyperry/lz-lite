package run.perry.lz.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import coil.load
import run.perry.lz.R
import run.perry.lz.player.PlayState

class SpinCoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mUrl: String? = null
    private var rotationAngle = 0f
    private var isManuallyPaused = false

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 20000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            if (!isManuallyPaused) {
                rotationAngle = it.animatedValue as Float
                rotation = rotationAngle
            }
        }
    }

    fun load(url: String?, placeholder: Int? = R.drawable.ic_album) {
        if (mUrl == url) return
        load(url) {
            error(placeholder ?: R.drawable.ic_album)
            placeholder(placeholder ?: R.drawable.ic_album)
        }
        mUrl = url
    }

    fun start() {
        isManuallyPaused = false
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        } else {
            valueAnimator.resume()
        }
    }

    fun pause() {
        isManuallyPaused = true
        if (valueAnimator.isRunning) {
            valueAnimator.pause()
        }
    }

    fun stop() {
        isManuallyPaused = true
        valueAnimator.cancel()
        rotation = 0f
        rotationAngle = 0f
    }

    fun release() {
        valueAnimator.cancel()
        valueAnimator.removeAllUpdateListeners()
    }

    fun syncWithState(state: PlayState) {
        when (state) {
            PlayState.Idle -> stop()
            PlayState.Pause -> pause()
            PlayState.Playing -> start()
            PlayState.Preparing -> pause()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec) // 保持正方形
    }
}
