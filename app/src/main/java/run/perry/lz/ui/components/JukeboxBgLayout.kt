package run.perry.lz.ui.components

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import coil.Coil
import coil.request.ImageRequest
import coil.size.Scale
import run.perry.lz.R
import run.perry.lz.utils.BitmapBlurHelper
import run.perry.lz.utils.asColor

class JukeboxBgLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var mImageViewBg: AppCompatImageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
    private var mImageViewFg: AppCompatImageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private var mBgRunnable: SetBgRunnable? = null
    private var mObjectAnimator: ObjectAnimator? = null

    init {
        mImageViewBg.setBackgroundResource(R.drawable.shape_jukebox_bg_default)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.JukeboxBgLayout)
            val isEnable = typedArray.getBoolean(R.styleable.JukeboxBgLayout_backgroundEnable, false)
            typedArray.recycle()
            if (isEnable) {
                initObjectAnimator()
            }
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun initObjectAnimator() {
        if (mObjectAnimator == null) {
            mObjectAnimator = ObjectAnimator.ofFloat(this, "number", 0f, 1.0f).apply {
                duration = 500L
                interpolator = AccelerateInterpolator()
                addUpdateListener { animation ->
                    mImageViewFg.let {
                        val foregroundAlpha = (animation.animatedValue as Float * 255).toInt()
                        it.drawable.mutate().alpha = foregroundAlpha
                    }
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        mImageViewFg.drawable?.let { drawable ->
                            mImageViewBg.setImageDrawable(drawable)
                        }
                    }

                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationRepeat(p0: Animator) {}
                })
            }
        }
    }

    fun setBackgroundCover(
        imageUrl: String,
        delayMillis: Long,
        isBlur: Boolean = true,
        blurRadius: Int = 25,
        shadeEnable: Boolean = true
    ) {
        if (mBgRunnable?.mImageUrl == imageUrl) return

        mBgRunnable?.let {
            it.onReset()
            removeCallbacks(it)
        }

        mBgRunnable = SetBgRunnable(isBlur, shadeEnable, imageUrl, blurRadius).apply {
            postDelayed(this, delayMillis)
        }
    }

    override fun setForeground(drawable: Drawable?) {
        mImageViewFg.setImageDrawable(drawable)
        startGradualAnimator()
    }

    private fun startGradualAnimator() {
        mObjectAnimator?.start()
    }

    private fun getForegroundDrawable(bitmap: Bitmap, radius: Int, filterColor: Int): Drawable? {
        if (radius <= 0) return null
        // 高斯模糊的实现
        val blurBitmap = BitmapBlurHelper.blur(bitmap, radius)
        val foregroundDrawable = blurBitmap?.toDrawable(resources)
        @Suppress("DEPRECATION")
        foregroundDrawable?.setColorFilter(filterColor, PorterDuff.Mode.MULTIPLY)
        return foregroundDrawable
    }

    private inner class SetBgRunnable(
        private val isBlur: Boolean,
        private val shadeEnable: Boolean,
        var mImageUrl: String?,
        private val blurRadius: Int
    ) : Runnable {

        override fun run() {
            try {
                val context = context ?: return
                if (mImageUrl.isNullOrBlank()) return

                val request = ImageRequest.Builder(context)
                    .data(mImageUrl)
                    .target { result ->

                        // 如果是 BitmapDrawable，直接获取 Bitmap
                        val bitmap = (result as? BitmapDrawable)?.bitmap ?: result.toBitmap()

                        if (isBlur) {
                            val drawable = getForegroundDrawable(
                                bitmap,
                                blurRadius,
                                (if (shadeEnable) R.color.black_9 else R.color.black).asColor
                            )
                            mImageViewFg.setImageDrawable(
                                drawable ?: ContextCompat.getDrawable(
                                    context,
                                    R.drawable.shape_jukebox_bg_default
                                )
                            )
                        } else {
                            mImageViewFg.setImageDrawable(bitmap.toDrawable(context.resources))
                        }

                        startGradualAnimator()
                    }
                    .scale(Scale.FILL)
                    .build()

                Coil.imageLoader(context).enqueue(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun onReset() {
            mImageUrl = null
        }
    }
}
