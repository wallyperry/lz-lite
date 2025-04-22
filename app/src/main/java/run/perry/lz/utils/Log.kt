package run.perry.lz.utils

import android.util.Log

object Log {
    private const val TAG = "LzLite"

    fun i(message: String, throwable: Throwable? = null) =
        Log.i(TAG, message, throwable)

    fun w(message: String, throwable: Throwable? = null) =
        Log.w(TAG, message, throwable)

    fun e(message: String, throwable: Throwable? = null) =
        Log.e(TAG, message, throwable)

    fun d(message: String, throwable: Throwable? = null) =
        Log.d(TAG, message, throwable)

    fun v(message: String, throwable: Throwable? = null) =
        Log.v(TAG, message, throwable)

    fun f(message: String, throwable: Throwable) =
        Log.wtf(message, throwable)
}
