@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.realm.plugin)
    alias(libs.plugins.build.konfig)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm {
        compilerOptions.jvmTarget.set(
            JvmTarget.fromTarget(libs.versions.jvmTarget.get())
        )
    }
    androidTarget {
        dependencies {
            // Chucker
            releaseImplementation(libs.chucker.noOp)
            debugImplementation(libs.chucker.debug)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                // Decompose
                api(libs.decompose.decompose)
                api(libs.essenty.lifecycle)

                // Compose
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.material3)

                // kotlinX
                implementation(libs.kotlinx.serialization.json)

                // Coil3
                api(libs.coil)
                api(libs.coil.compose)
                api(libs.coil.network)

                // Koin
                api(libs.koin.core)

                // Realm
                implementation(libs.realm.library.base)

                // Multiplatform Settings
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.serialization)
                implementation(libs.multiplatform.settings.coroutines)

                // Ficbook api
                implementation(projects.ficbookApi)

                // features
                api(projects.features.fileDownloadFeature)
                api(projects.features.mpfilepicker)
                api(projects.features.fanficDownloader)
            }
        }
        jvmMain {
            dependencies {

            }
        }
        androidMain {
            dependencies {
                // AndroidX
                implementation(libs.androidx.browser)

                // koin
                implementation(libs.koin.android)
            }
        }
    }
}

android {
    namespace = "ru.blays.ficbook.reader"
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

val proxyPropsFile = rootProject.file("proxy.properties")
val proxyProps = Properties().apply {
    load(proxyPropsFile.reader())
}

buildkonfig {
    packageName = libs.versions.applicationId.get()
    exposeObjectWithName = "SharedBuildKonfig"

    defaultConfigs {
        // Application info
        buildConfigField(STRING, "versionName", libs.versions.projectVersion.get(), const = true)
        buildConfigField(STRING, "versionNameFull", libs.versions.projectVersion.get() + libs.versions.versionNameSuffix.get(), const = true)
        buildConfigField(STRING, "versionCode", libs.versions.versionCode.get(), const = true)
        buildConfigField(STRING, "applicationId", libs.versions.applicationId.get(), const = true)

        // Proxy config
        buildConfigField(STRING, "proxyHost", proxyProps["hostname"] as String, const = true)
        buildConfigField(INT, "proxyPort", proxyProps["port"] as String, const = true)
        buildConfigField(STRING, "proxyType", proxyProps["type"] as String, const = true)
        buildConfigField(STRING, "proxyUsername", proxyProps["username"] as String, const = true)
        buildConfigField(STRING, "proxyPassword", proxyProps["password"] as String, const = true)
    }
}
