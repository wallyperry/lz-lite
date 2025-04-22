package run.perry.lz.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import run.perry.lz.R
import run.perry.lz.data.AppStore
import run.perry.lz.data.ENV
import run.perry.lz.utils.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


class SplashView @JvmOverloads constructor(
    private val activity: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(activity, attrs, defStyleAttr) {

    private var durationTime: Int = 5
    private var defaultBitmapRes: Int = R.mipmap.pic_splash
    private lateinit var imageClickCallback: (String?) -> Unit
    private lateinit var dismissCallback: (Boolean) -> Unit
    private val splashSkipButtonBg = GradientDrawable()
    private var splashImageView: AppCompatImageView? = null
    private var skipButton: AppCompatTextView? = null
    private val SKIP_BUTTON_SIZE_IN_DIP: Int = 36
    private val SKIP_BUTTON_MARGIN_IN_DIP: Int = 16
    private var duration = 6
    private val DELAY_TIME: Int = 1000
    private var IMG_PATH = ENV.APP.SPLASH_CACHE_PATH
    private var actUrl: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (0 == duration) {
                dismissSplashView(false)
                return
            } else {
                setDuration(--duration)
            }
            handler.postDelayed(timerRunnable, DELAY_TIME.toLong())
        }
    }

    constructor(
        activity: Activity,
        statusBarHeight: Int,
        durationTime: Int,
        defaultBitmapRes: Int,
        imageClickCallback: (String?) -> Unit,
        dismissCallback: (Boolean) -> Unit
    ) : this(activity, null, 0) {

        this.durationTime = durationTime
        this.defaultBitmapRes = defaultBitmapRes
        this.imageClickCallback = imageClickCallback
        this.dismissCallback = dismissCallback

        splashSkipButtonBg.apply {
            shape = GradientDrawable.OVAL
            setColor("#66333333".toColorInt())
        }

        splashImageView = AppCompatImageView(activity).apply {
            scaleType = ScaleType.CENTER_CROP
            setBackgroundColor(Color.WHITE)
            this@SplashView.addView(this)
            isClickable = true
        }

        skipButton = AppCompatTextView(activity).apply {
            val skipBtnSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                SKIP_BUTTON_SIZE_IN_DIP.toFloat(),
                activity.resources.displayMetrics
            ).toInt()
            val params = LayoutParams(skipBtnSize, skipBtnSize).apply {
                gravity = Gravity.TOP or Gravity.END
                val skipButtonMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    SKIP_BUTTON_MARGIN_IN_DIP.toFloat(),
                    activity.resources.displayMetrics
                ).toInt()
                setMargins(0, skipButtonMargin + statusBarHeight, skipButtonMargin, 0)
            }
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = splashSkipButtonBg
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            this@SplashView.addView(this, params)
            setOnClickListener { dismissSplashView(true) }
            setDuration(duration)
        }
    }

    private fun setDuration(duration: Int) {
        this.duration = duration
        skipButton?.text = String.format(Locale.CHINA, "跳过\n%d s", duration)
    }

    private fun dismissSplashView(initiativeDismiss: Boolean) {
        dismissCallback.invoke(initiativeDismiss)
        handler.removeCallbacks(timerRunnable)
    }

    fun show() {
        val contentView: ViewGroup? =
            (activity as Activity).window.decorView.findViewById(android.R.id.content)
        if (contentView?.childCount == 0) throw IllegalStateException("You should call show() after setContentView() in Activity instance")
        val param = RelativeLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )

        splashImageView?.setOnClickListener { imageClickCallback.invoke(actUrl) }
        durationTime.run { this@SplashView.setDuration(this) }

        var bitmapToShow: Bitmap? = null
        if (isExistsLocalSplashData()) {
            bitmapToShow = BitmapFactory.decodeFile(IMG_PATH)
            actUrl = AppStore.splashActUrl
        } else {
            bitmapToShow = BitmapFactory.decodeResource(activity.resources, defaultBitmapRes)
        }

        if (bitmapToShow == null) return

        splashImageView?.setImageBitmap(bitmapToShow)
        contentView?.addView(this@SplashView, param)
    }

    fun updateData(imgUrl: String?, actUrl: String?) {
        AppStore.splashImgUrl = imgUrl.orEmpty()
        AppStore.splashActUrl = actUrl.orEmpty()
        Log.i("save splash image url: $imgUrl")
        Log.i("save splash action url: $actUrl")
        if (!imgUrl.isNullOrBlank()) downloadImage(imgUrl)
    }

    fun startCountDown() {
        this@SplashView.handler.postDelayed(timerRunnable, DELAY_TIME.toLong())
    }

    private fun downloadImage(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.i("download splash file fail", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.i("download splash file fail, response is null")
                    return
                }

                response.body.byteStream().use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    saveBitmapFile(bitmap, IMG_PATH)
                    Log.i("download & save splash file: $IMG_PATH")
                }
            }
        })
    }

    private fun saveBitmapFile(bitmap: Bitmap, filePath: String) {
        val file = File(filePath)
        file.parentFile?.mkdirs() // 确保目录存在

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }
    }

    private fun isExistsLocalSplashData() =
        AppStore.splashImgUrl.isNotEmpty() && isFileExists(IMG_PATH)

    private fun isFileExists(filepath: String): Boolean {
        if (filepath.isEmpty()) return false
        val file = File(filepath)
        return file.exists() && file.isFile
    }
}