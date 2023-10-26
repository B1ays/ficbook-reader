
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.gradle.versions.plugin)
}


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.libres.plugin)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}