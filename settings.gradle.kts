enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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
include("features")
include("features:fileDownloadFeature")
include("features:mpfilepicker")
include("features:copyImageFeature")
include("features:fanficDownloader")

includeBuild("./haze") {
    name = "haze-root"
    dependencySubstitution {
        substitute(
            module("dev.chrisbanes.haze:haze")
        ).using(project(":haze"))
        substitute(
            module("dev.chrisbanes.haze:haze-materials")
        ).using(project(":haze-materials"))
    }
}

includeBuild("../epub4j-kotlin") {
    name = "epub4j"
    dependencySubstitution {
        substitute(
            module("io.documentnode.epub4j:epub4j-core")
        ).using(project(":epub4j-core"))
    }
}
includeBuild("../constraintlayout-compose-multiplatform") {
    name = "constraintlayout-compose-multiplatform"
    dependencySubstitution {
        substitute(
            module("tech.annexflow.compose:compose")
        ).using(project(":compose"))
    }
}