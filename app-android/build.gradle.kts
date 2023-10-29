@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
}

android {
    namespace = "ru.blays.ficbook.reader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.blays.ficbook.reader"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = libs.versions.project.get()

        ndk {
            abiFilters += setOf("arm64-v8a")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.activityCompose)

    // Compose
    implementation(compose.foundation)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Project
    implementation(projects.shared)
    implementation(projects.composeUi)
}