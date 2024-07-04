plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                // KotlinX
                implementation(libs.kotlinx.coroutines.core.jvm)
                implementation(libs.kotlinx.serialization.json)

                // JSoup
                implementation(libs.jsoup)

                // OkHttp
                api(libs.okhttp)

                // Koin
                implementation(libs.koin.core)
            }
        }
    }
}