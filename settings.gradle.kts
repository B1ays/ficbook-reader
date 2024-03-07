enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    versionCatalogs {
        create("hazeLibs") {
            from(files("gradle/hazeLibs.versions.toml"))
        }
    }
}

rootProject.name = "ficbook-reader"
include(":shared")
include(":compose-ui")
include(":app-android")
include(":app-desktop")
include(":ficbookApi")

include(":haze:haze", ":haze:haze-materials")
project(":haze:haze").projectDir = file("${rootDir}/haze/haze")
project(":haze:haze-materials").projectDir = file("${rootDir}/haze/haze-materials")

include("features")
include("features:fileDownloadFeature")
include("features:mpfilepicker")
include("features:copyImageFeature")
findProject(":features:copyImageFeature")?.name = "copyImageFeature"
