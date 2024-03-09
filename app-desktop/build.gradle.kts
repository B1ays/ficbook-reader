import org.jetbrains.compose.desktop.application.dsl.TargetFormat

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
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
                implementation(projects.shared)
                implementation(projects.composeUi)

                implementation(compose.desktop.currentOs)
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.material3)

                implementation(libs.decompose.extensionsComposeJetbrains)

                implementation(libs.kotlinx.coroutines.core.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "ru.blays.ficbook.reader.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Ficbook reader"
            packageVersion = libs.versions.projectVersion.get()
            includeAllModules = true

            windows {
                iconFile.set(File("src/jvmMain/resources/icon_windows.ico"))
            }
        }
    }
}
