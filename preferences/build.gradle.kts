plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

@Suppress("OPT_IN_USAGE")
kotlin {
    targetHierarchy.default()
    jvm()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // AndroidX
                implementation(libs.androidx.datastore.core)

                // KotlinX
                implementation(libs.kotlinx.serialization.json)

                // Compose
                implementation(compose.runtime)
                implementation(compose.ui)

                // koin
                implementation(libs.koin.core)
            }
        }
        val androidMain by getting {
            dependencies {
                // AndroidX
                implementation(libs.androidx.datastore)

                // koin
                implementation(libs.koin.android)
            }
        }
    }
}

android {
    namespace = "ru.blays.preference"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}
