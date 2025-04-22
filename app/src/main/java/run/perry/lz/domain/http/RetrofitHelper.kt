package run.perry.lz.domain.http

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import run.perry.lz.BuildConfig
import run.perry.lz.data.AppStore
import run.perry.lz.domain.http.api.ApiService
import run.perry.lz.domain.http.api.RawService
import run.perry.lz.domain.http.utils.baseUrlInterceptor
import run.perry.lz.domain.http.utils.customHostnameVerifier
import run.perry.lz.domain.http.utils.customSslSocketFactory
import run.perry.lz.domain.http.utils.customTrustManager
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

object RetrofitHelper {

    val apiService by lazy { getServiceOfGson(ApiService::class.java) }
    val rawService by lazy { getServiceOfScalars(RawService::class.java) }

    private var gsonRetrofit: Retrofit? = null
    private var scalarsRetrofit: Retrofit? = null

    private val client = OkHttpClient.Builder().apply {
        callTimeout(10, TimeUnit.SECONDS)
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)

        baseUrlInterceptor()

        if (BuildConfig.DEBUG) {
            addNetworkInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
        }

        sslSocketFactory(
            customSslSocketFactory(),
            customTrustManager()[0] as X509TrustManager
        )
        hostnameVerifier(customHostnameVerifier())
        connectionSpecs(
            listOf(
                ConnectionSpec.CLEARTEXT,
                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .allEnabledTlsVersions()
                    .allEnabledCipherSuites()
                    .build()
            )
        )
    }.build()

    fun <T> getServiceOfGson(serviceClass: Class<T>): T {
        if (gsonRetrofit == null) {
            gsonRetrofit = Retrofit.Builder()
                .baseUrl(AppStore.baseurl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return gsonRetrofit!!.create(serviceClass)
    }

    fun <T> getServiceOfScalars(serviceClass: Class<T>): T {
        if (scalarsRetrofit == null) {
            scalarsRetrofit = Retrofit.Builder()
                .baseUrl(AppStore.baseurl)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
        return scalarsRetrofit!!.create(serviceClass)
    }
}