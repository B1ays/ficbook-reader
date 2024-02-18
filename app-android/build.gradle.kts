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
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.projectVersion.get()
        versionNameSuffix = libs.versions.versionNameSuffix.get()

        proguardFile("proguard-rules.pro")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    //noinspection ChromeOsAbiSupport
    buildTypes {
        release {
            //TODO Configure Proguard
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                abiFilters += setOf("armeabi-v7a")
            }
        }
        debug {
            ndk {

                abiFilters += setOf("arm64-v8a", "x86")
            }
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