plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
                // Koin
                implementation(libs.koin.core)

                // epub4j
                implementation(projects.epub4j.epub4jCore) {
                    exclude(group = "xmlpull")
                }
                /*implementation("io.documentnode:epub4j-core:4.2.1") {
                    exclude(group = "xmlpull")
                }*/

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
    targets.all {
        compilations.all {
            compilerOptions.configure {
                allWarningsAsErrors = false
            }
        }
    }
}

android {
    namespace = "ru.blays.ficbook.features.fanficDownloader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}