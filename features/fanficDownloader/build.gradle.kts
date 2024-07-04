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

                // epub4j
                implementation("io.documentnode.epub4j:epub4j-core") {
                    exclude(group = "xmlpull")
                }

                implementation(projects.features.mpfilepicker)
                implementation(projects.ficbookApi)


            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.core)
                implementation(libs.androidx.work.ktx)
            }
        }
    }

    compilerOptions {
        allWarningsAsErrors = false
    }
}

android {
    namespace = "ru.blays.ficbook.features.fanficDownloader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}