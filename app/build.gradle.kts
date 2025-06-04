import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "run.perry.lz"
    compileSdk = 36

    defaultConfig {
        applicationId = "run.perry.lz"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        val buildTimeFormat: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date())
        resValue("string", "build_time", buildTimeFormat)
    }

    signingConfigs {
        //默认签名
        create("defaultSigningConfig") {
            storeFile = file("../keystore/wallyperry.jks")
            storePassword = "worinima"
            keyAlias = "android"
            keyPassword = "worinima"
        }
    }

    productFlavors {
        flavorDimensions("env")

        create("official") {
            resValue("string", "app_name", "栗子音乐")
            buildConfigField(
                "String", "GATEWAY_ADDRESS",
                "\"https://gitee.com/wallyperry/lz/raw/master/config.json\""
            )
        }

        create("beta") {
            applicationIdSuffix = ".beta"
            resValue("string", "app_name", "栗子Beta")
            buildConfigField(
                "String", "GATEWAY_ADDRESS",
                "\"https://gitee.com/wallyperry/lz/raw/master/config.json\""
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            productFlavors.all {
                signingConfig =
                    signingConfig ?: signingConfigs.getByName("defaultSigningConfig")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    applicationVariants.all {
        val variant = this
        val buildType = this.buildType.name
        val buildFlavor = this.productFlavors[0].name
        val buildTime: String = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA).format(Date())
        outputs.all {
            if (this is ApkVariantOutputImpl) {
                outputFileName =
                    "Lzmusic_${buildFlavor}_v${variant.versionName}_${buildTime}_${buildType}.apk"
            }
        }
    }

}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewmodel.ktx)
    implementation(libs.androidx.swiperefresh)
    implementation(libs.android.material)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.debug.db)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.retrofit.scalars)

    implementation(libs.koin)
    implementation(libs.coil)
    implementation(libs.lottie)
    implementation(libs.updateapp)
    implementation(libs.immersionbar)
    implementation(libs.immersionbar.ktx)

    implementation(libs.github.shadowlayout)
    implementation(libs.github.autosize)
    implementation(libs.github.toaster)
    implementation(libs.github.banner)
    implementation(libs.github.brvah)
    implementation(libs.github.lyric)
    implementation(libs.github.fadingedge)

    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.media3.exo)
    implementation(libs.media3.exo.dash)
    implementation(libs.media3.exo.hls)
    implementation(libs.media3.exo.workmanager)
    implementation(libs.media3.database)
    implementation(libs.media3.datasource)
    implementation(libs.media3.datasource.rtmp)
    implementation(libs.media3.datasource.cronet)
    implementation(libs.media3.datasource.okhttp)

    implementation(libs.videocache)

    implementation(libs.umeng.common)
    implementation(libs.umeng.asms)
}