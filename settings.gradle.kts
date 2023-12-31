pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "ficbook-reader"
include(":shared")
include(":compose-ui")
include(":app-android")
include(":app-desktop")
include(":ficbookApi")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

