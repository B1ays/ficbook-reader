plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // KotlinX
                implementation(libs.kotlinx.coroutines.core.jvm)
                implementation(libs.kotlinx.serialization.json)

                // JSoup
                api(libs.jsoup)

                // OkHttp
                api(libs.okhttp)

                // Koin
                api(libs.koin.core)
            }
        }
    }
}