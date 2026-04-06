// build.gradle.kts (app)
// Todas las dependencias necesarias para la Fase 1.
// Las de Fase 2+ están comentadas — las descomentás cuando las necesites.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)          // Hilt
    alias(libs.plugins.ksp)           // KSP para Room y Hilt
    alias(libs.plugins.google.services)  // Firebase
}

android {
    namespace = "com.example.localservice"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.localservice"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
}

dependencies {

    // --- Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- ViewModel + Lifecycle ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // --- Hilt (inyección de dependencias) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Firebase (BOM sincroniza versiones automáticamente) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)   // FCM — notificaciones push

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services) // para .await() en Firebase

    // --- Room (cache local) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Coil (imágenes) ---
    implementation(libs.coil.compose)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // FASE 2+ — descomentá cuando lo necesites:
    // implementation(libs.retrofit)
    // implementation(libs.retrofit.converter.gson)
    // implementation(libs.okhttp.logging.interceptor)
    // implementation(libs.maps.compose)
    // implementation(libs.play.services.location)
}
