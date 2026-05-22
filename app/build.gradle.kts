import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

val localProperties = Properties()

localProperties.load(
    rootProject.file("local.properties").inputStream()
)

android {
    namespace = "com.tech.motjip"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.tech.motjip"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            "\"${localProperties.getProperty("KAKAO_NATIVE_APP_KEY")}\""
        )

        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")

            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("com.google.firebase:firebase-messaging:24.0.1")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("org.jsoup:jsoup:1.14.3")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-codec:commons-codec:1.15")

    implementation("com.google.dagger:hilt-android:2.56.1")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.56.1")

    implementation("com.kakao.maps.open:android:2.13.1")
    implementation("com.kakao.sdk:v2-user:2.20.6")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.exifinterface:exifinterface:1.3.7")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("androidx.browser:browser:1.8.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("it.xabaras.android:recyclerview-swipedecorator:1.4")

    implementation("com.github.yalantis:ucrop:2.2.8")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}