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
    }
}

rootProject.name = "ficbook-reader"
include(":shared")
include(":compose-ui")
include(":app-android")
include(":app-desktop")
include(":ficbookApi")
include("preferences")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

