package run.perry.lz.domain.http.utils

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 获取SSLSocketFactory
 */
fun OkHttpClient.Builder.customSslSocketFactory(): SSLSocketFactory {
    try {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, customTrustManager(), SecureRandom())
        return sslContext.socketFactory
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

/**
 * 获取TrustManager
 */
fun OkHttpClient.Builder.customTrustManager() = arrayOf<TrustManager>(
    @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?,
        ) = Unit

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?,
        ) = Unit

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

/**
 * 获取HostnameVerifier
 */
fun OkHttpClient.Builder.customHostnameVerifier() = HostnameVerifier { _, _ -> true }