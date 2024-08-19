import org.gradle.api.internal.DocumentationRegistry.BASE_URL

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.aplikasistoryapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aplikasistoryapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"${BASE_URL}\"")
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {

    // Core AndroidX dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.adapter.rxjava2)

    // DataStore
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Glide
    implementation(libs.glide)
    androidTestImplementation(project(":app"))
    annotationProcessor(libs.compiler)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)

    // Maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Lottie Animation
    implementation(libs.lottie)

    // Other Libraries
    implementation(libs.justifiedtextview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.core)
    testImplementation(libs.androidx.paging.common.ktx)
    testImplementation (libs.androidx.core.testing)
    testImplementation (libs.robolectric.v4103)
    testImplementation (libs.mockito.core.v530)
    testImplementation (libs.mockito.inline.v520)

    // UI Testing dependencies
    androidTestImplementation(libs.androidx.junit.v113)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(libs.androidx.espresso.idling.resource)
    androidTestImplementation(libs.androidx.espresso.intents.v351)
    androidTestImplementation(libs.androidx.espresso.contrib)
}