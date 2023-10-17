//@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    kotlin("plugin.serialization") version "1.9.10"
}

@Suppress("OPT_IN_USAGE")
kotlin {
    targetHierarchy.default()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.core.jvm)
                implementation(libs.kotlinx.serialization.json)

                // Apache
                implementation(libs.commons.text)

                // KSoup
                api(libs.jsoup)

                // OkHttp
                api(libs.okhttp)
            }
        }
    }
}