@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

group = "ru.blays"
version = "1"

kotlin {
    applyDefaultHierarchyTemplate()
    jvm {
        compilerOptions.jvmTarget.set(
            JvmTarget.fromTarget(libs.versions.jvmTarget.get())
        )
    }
    androidTarget()

    sourceSets {
        commonMain {
            dependencies {
                // Compose
                implementation(compose.ui)

                // Coil
                implementation(libs.coil.compose)

                // Koin
                implementation(libs.koin.core)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.core)
            }
        }
    }

    compilerOptions {
        allWarningsAsErrors = false
    }
}

android {
    namespace = "ru.blays.ficbook.features.copyImageFeature"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}