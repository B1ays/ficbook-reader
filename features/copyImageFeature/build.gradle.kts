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
    jvm()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }

    }
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
    targets.all {
        compilations.all {
            compilerOptions.configure {
                allWarningsAsErrors = false
            }
        }
    }
}

android {
    namespace = "ru.blays.ficbook.features.copyImageFeature"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}