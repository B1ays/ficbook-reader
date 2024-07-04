@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
                // Koin
                implementation(libs.koin.core)

                // OkHttp
                implementation(libs.okhttp)

                // File picker
                implementation(projects.features.mpfilepicker)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.core)
            }
        }
        jvmMain {
            val lwjglVersion = libs.versions.lwjgl.get()
            dependencies {
                listOf("lwjgl", "lwjgl-tinyfd").forEach { lwjglDep ->
                    implementation("org.lwjgl:${lwjglDep}:${lwjglVersion}")
                    listOf(
                        "natives-windows",
                        "natives-windows-x86",
                        "natives-windows-arm64",
                        "natives-macos",
                        "natives-macos-arm64",
                        "natives-linux",
                        "natives-linux-arm64",
                        "natives-linux-arm32"
                    ).forEach { native ->
                        runtimeOnly("org.lwjgl:${lwjglDep}:${lwjglVersion}:${native}")
                    }
                }
            }
        }
    }

    compilerOptions {
        allWarningsAsErrors = false
    }
}

android {
    namespace = "ru.blays.ficbook.features.fileDownloadFeature"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        proguardFile("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}