import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    load(keystorePropsFile.reader())
}

android {
    namespace = "ru.blays.ficbook.reader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.projectVersion.get()
        libs.versions.versionNameSuffix.get()
            .takeIf(String::isNotBlank)
            ?.let(::versionNameSuffix::set)

        resourceConfigurations += "ru"

        proguardFile("proguard-rules.pro")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += arrayOf("abi")

    signingConfigs {
        create("release") {
            keyAlias = keystoreProps["keyAlias"] as String
            keyPassword = keystoreProps["keyPassword"] as String
            storeFile = rootProject.file(keystoreProps["storeFile"] as String)
            storePassword = keystoreProps["storePassword"] as String
        }
    }

    //noinspection ChromeOsAbiSupport
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            ndk {
                abiFilters += setOf("arm64-v8a", "x86")
            }
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    //noinspection ChromeOsAbiSupport
    productFlavors {
        create("arm7") {
            dimension = "abi"
            ndk {
                abiFilters += setOf("armeabi-v7a")
            }
        }
        create("arm64") {
            dimension = "abi"
            ndk {
                abiFilters += setOf("arm64-v8a")
            }
        }
        create("x86") {
            dimension = "abi"
            ndk {
                abiFilters += setOf("x86")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
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