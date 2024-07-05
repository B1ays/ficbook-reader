import org.jetbrains.compose.desktop.application.dsl.TargetFormat

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.realm.plugin)
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.components.resources)
            }
        }
        jvmMain {
            dependencies {
                // Compose
                implementation(compose.desktop.currentOs)
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.material3)

                // KotlinX
                implementation(libs.kotlinx.coroutines.core.swing)

                // Decompose
                implementation(libs.decompose.extensionsComposeJetbrains)

                // Realm
                implementation(libs.realm.library.base)

                // Modules
                implementation(projects.shared)
                implementation(projects.composeUi)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "ru.blays.ficbook.reader.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb
            )
            packageName = "Ficbook reader"
            packageVersion = libs.versions.projectVersion.get()
            includeAllModules = true

            windows {
                iconFile.set(File("src/jvmMain/resources/icon_windows.ico"))
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))

            //obfuscate.set(true)
            optimize.set(true)
            joinOutputJars.set(true)
        }
    }
}
