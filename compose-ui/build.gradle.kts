@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

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
                // Compose Libraries
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.animation)
                implementation(compose.runtimeSaveable)
                implementation(compose.components.resources)

                // KotlinX
                implementation(libs.kotlinx.serialization.json)

                // ConstraintLayout
                implementation(libs.compose.constraintLayout.get().toString()) {
                    exclude(group = "org.jetbrains.compose.annotation-internal")
                }


                // materialKolor
                implementation(libs.materialKolor)

                // Haze
                implementation(libs.haze)

                // Color-picker
                implementation(libs.compose.color.picker)

                // Decompose Libraries
                implementation(libs.decompose.decompose)
                implementation(libs.decompose.extensionsComposeJetbrains)

                // Rebugger
                implementation(libs.rebugger)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // Modules
                implementation(projects.shared)
                implementation(projects.features.copyImageFeature)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.compose.color.picker.jvm)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.activity.activityCompose)

                implementation(libs.compose.color.picker.android)
            }
        }
    }

    compilerOptions {
        allWarningsAsErrors = false
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
}

android {
    namespace = "com.example.myapplication.compose"
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

composeCompiler {
    this.enableStrongSkippingMode = true
    this.enableIntrinsicRemember = true
    this.enableNonSkippingGroupOptimization = true
}