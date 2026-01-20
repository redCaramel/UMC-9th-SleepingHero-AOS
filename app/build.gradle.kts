import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.umc_9th.sleepinghero"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.umc_9th.sleepinghero"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        //local property
        val properties = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }

        val configValues = mapOf(
            "KAKAO_NATIVE_KEY" to properties.getProperty("KAKAO_NATIVE_KEY"),
            "NAVER_CLIENT_ID" to properties.getProperty("NAVER_CLIENT_ID"),
            "NAVER_CLIENT_SECRET" to properties.getProperty("NAVER_CLIENT_SECRET")
        )
        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"${configValues["KAKAO_NATIVE_KEY"]}\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${configValues["NAVER_CLIENT_ID"]}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${configValues["NAVER_CLIENT_SECRET"]}\"")

        manifestPlaceholders.putAll(configValues)
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Kakao SDK
    implementation("com.kakao.sdk:v2-user:2.23.0")
    implementation("com.kakao.sdk:v2-auth:2.23.0")

    // Naver SDK
    implementation("com.navercorp.nid:oauth-jdk8:5.1.0")
}