package run.perry.lz.domain.http.utils

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient

/**
 * 动态 BaseUrl 拦截器
 * 示例：
 * interface ApiService {
 *     @GET("users")
 *     fun getUsers(@Header("baseurl") baseurl: String): Call<List<User>>
 * }
 */
fun OkHttpClient.Builder.baseUrlInterceptor() {
    addInterceptor { chain ->
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        // 获取请求头中的 baseurl
        val newBaseUrl = originalRequest.header("baseurl")

        // 如果 baseurl 存在，则修改请求的 URL
        val newRequest = if (!newBaseUrl.isNullOrBlank()) {
            val newHttpUrl = newBaseUrl.toHttpUrlOrNull()?.newBuilder()
                ?.addPathSegments(originalHttpUrl.encodedPath.substring(1))
                ?.encodedQuery(originalHttpUrl.encodedQuery)
                ?.build()

            originalRequest.newBuilder()
                .url(newHttpUrl ?: originalHttpUrl)
                .removeHeader("baseurl")
                .build()
        } else {
            originalRequest
        }

        // 执行修改后的请求
        chain.proceed(newRequest)
    }.build()
}