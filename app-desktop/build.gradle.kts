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
        val jvmMain by getting {
            dependencies {
                implementation(projects.shared)
                implementation(projects.composeUi)

                implementation(compose.desktop.currentOs)
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(libs.decompose.extensionsComposeJetbrains)

            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "ru.blays.ficbookReader.desktop.MainKt"


        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Ficbook reader"
            packageVersion = libs.versions.project.get()
            includeAllModules = true
        }
    }
}
