@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.library)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    alias(libs.plugins.realm.plugin)
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
                // Decompose
                api(libs.decompose.decompose)
                api(libs.essenty.lifecycle)

                // Compose
                implementation(compose.runtime)
                implementation(compose.ui)

                // kotlinX
                implementation(libs.kotlinx.serialization.json)

                // Kamel
                api(libs.kamel.image)

                // Koin
                api(libs.koin.core)

                // Realm
                implementation(libs.realm.library.base)

                // Ficbook api
                implementation(projects.ficbookApi)
            }
        }
        val jvmMain by getting {
            dependencies {
                // ObjectBox Desktop
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                //implementation(libs.koin.androidx)
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
    }
}

android {
    namespace = "ru.blays.ficbook.reader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}