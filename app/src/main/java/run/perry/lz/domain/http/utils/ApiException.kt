package run.perry.lz.domain.http.utils

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * 错误处理函数
 */
fun parseThrowable(e: Throwable) = when (e) {
    // 网络错误，通常由于没有网络或网络中断
    is IOException -> when (e) {
        is UnknownHostException -> "网络不可用，请检查您的网络连接"
        is ConnectException -> "无法连接到服务器，请检查您的网络设置"
        is SocketTimeoutException -> "请求超时，请稍后重试"
        else -> "网络连接失败，请检查网络设置"
    }
    // HTTP 错误，捕获响应错误状态码
    is HttpException -> {
        val code = e.code()
        when (code) {
            400 -> "请求参数错误"
            401 -> "未授权，请登录后重试"
            403 -> "权限不足，无法访问"
            404 -> "资源未找到"
            408 -> "请求超时，请稍后再试"
            422 -> "参数验证失败，请检查输入"
            500 -> "服务器错误，请稍后再试"
            502 -> "网关错误，请稍后再试"
            503 -> "服务不可用，请稍后再试"
            504 -> "请求超时，请稍后再试"
            else -> "请求失败，错误代码: $code"
        }
    }
    // 请求超时错误
    is TimeoutException -> "请求超时，请稍后重试"
    // JSON 解析错误
    is JsonParseException -> "数据解析失败，请稍后再试"
    // 其他未知异常
    else -> e.message ?: "未知错误"
}


