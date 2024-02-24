plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
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
                implementation(compose.runtime)
                implementation(compose.runtimeSaveable)

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
    targets.all {
        compilations.all {
            compilerOptions.configure {
                allWarningsAsErrors = false
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}