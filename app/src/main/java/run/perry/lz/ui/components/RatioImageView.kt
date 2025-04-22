package run.perry.lz.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import run.perry.lz.R

/**
 * @author Perry
 * @date 2022/6/21
 * TG https://t.me/wallyperry
 */
class RatioImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    /* 优先级从大到小：
     mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
     mWidthRatio mHeightRatio
     即如果设置了mIsWidthFitDrawableSizeRatio为true，则优先级较低的三个值不生效 */
    /**
     * src图片(前景图)的宽高比例
     */
    private var mDrawableSizeRatio = -1f

    /**
     * 根据前景图宽高比例测量View,防止图片缩放变形
     * mIsWidthFitDrawableSizeRatio：宽度是否根据src图片(前景图)的比例来测量（高度已知）
     * mIsHeightFitDrawableSizeRatio：高度是否根据src图片(前景图)的比例来测量（宽度已知）
     */
    private var mIsWidthFitDrawableSizeRatio = false
    private var mIsHeightFitDrawableSizeRatio = false

    /**
     * 宽高比例
     * mWidthRatio：宽度 = 高度*mWidthRatio
     * mHeightRatio：高度 = 宽度*mHeightRatio
     */
    private var mWidthRatio = -1f
    private var mHeightRatio = -1f

    init {
        context.withStyledAttributes(attrs, R.styleable.RatioImageView) {
            mIsWidthFitDrawableSizeRatio = getBoolean(
                R.styleable.RatioImageView_is_width_fix_drawable_size_ratio, false
            )
            mIsHeightFitDrawableSizeRatio = getBoolean(
                R.styleable.RatioImageView_is_height_fix_drawable_size_ratio, false
            )
            mHeightRatio = getFloat(
                R.styleable.RatioImageView_height_to_width_ratio, mHeightRatio
            )
            mWidthRatio = getFloat(
                R.styleable.RatioImageView_width_to_height_ratio, mWidthRatio
            )
        }
        // 一定要有此代码
        if (drawable != null) mDrawableSizeRatio =
            1f * drawable.intrinsicWidth / drawable.intrinsicHeight
    }

    /**
     * 高度设置，参考宽度，如0.5 , 表示 高度＝宽度×０.5
     */
    fun heightToWidth(ratio: Float) {
        mHeightRatio = ratio
        mWidthRatio = -1f
        requestLayout()
    }

    fun widthToHeight(ratio: Float) {
        mHeightRatio = -1f
        mWidthRatio = ratio
        requestLayout()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (drawable != null) {
            mDrawableSizeRatio = 1f * drawable.intrinsicWidth / drawable.intrinsicHeight
            if (mDrawableSizeRatio > 0) if (mIsWidthFitDrawableSizeRatio || mIsHeightFitDrawableSizeRatio)
                requestLayout()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (getDrawable() != null) {
            mDrawableSizeRatio = (1f * getDrawable().intrinsicWidth / getDrawable().intrinsicHeight)
            if (mDrawableSizeRatio > 0)
                if (mIsWidthFitDrawableSizeRatio || mIsHeightFitDrawableSizeRatio) requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 优先级从大到小：
        // mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
        // mWidthRatio mHeightRatio
        // 根据前景图宽高比例来测量view的大小
        if (mDrawableSizeRatio > 0)
            if (mIsWidthFitDrawableSizeRatio) mWidthRatio = mDrawableSizeRatio else
                if (mIsHeightFitDrawableSizeRatio) mHeightRatio = 1 / mDrawableSizeRatio
        if (mHeightRatio > 0 && mWidthRatio > 0) throw RuntimeException("高度和宽度不能同时设置百分比！！")
        when {
            mWidthRatio > 0 -> {
                // 高度已知，根据比例，设置宽度
                val height = MeasureSpec.getSize(heightMeasureSpec)
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(
                        (height * mWidthRatio).toInt(), MeasureSpec.EXACTLY
                    ),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                )
            }

            mHeightRatio > 0 -> {
                // 宽度已知，根据比例，设置高度
                val width = MeasureSpec.getSize(widthMeasureSpec)
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(
                        width,
                        MeasureSpec.EXACTLY
                    ), MeasureSpec.makeMeasureSpec(
                        (width * mHeightRatio).toInt(), MeasureSpec.EXACTLY
                    )
                )
            }
            // 系统默认测量
            else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}