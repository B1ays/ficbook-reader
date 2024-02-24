plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.gradle.cacheFix)
}

kotlin {
	applyDefaultHierarchyTemplate()
	androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
        }
	}
    jvm()

	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			api(compose.foundation)

			implementation(libs.koin.core)
		}

		androidMain.dependencies {
			implementation(compose.uiTooling)
			implementation(compose.preview)
			implementation(compose.material)
			implementation(libs.androidx.appcompat)
			implementation(libs.androidx.core)
			implementation(libs.androidx.activity.activityCompose)
			implementation(libs.kotlinx.coroutines.core.android)

			implementation(libs.koin.android)
		}

		jvmMain.dependencies {
			implementation(compose.uiTooling)
			implementation(compose.preview)
			implementation(compose.material)

			listOf("lwjgl", "lwjgl-tinyfd").forEach { lwjglDep ->
				implementation("org.lwjgl:${lwjglDep}:${libs.versions.lwjgl.get()}")
				listOf(
					"natives-windows",
					"natives-windows-x86",
					"natives-windows-arm64",
					"natives-macos",
					"natives-macos-arm64",
					"natives-linux",
					"natives-linux-arm64",
					"natives-linux-arm32"
				).forEach { native ->
					runtimeOnly("org.lwjgl:${lwjglDep}:${libs.versions.lwjgl.get()}:${native}")
				}
			}
		}
	}

	@Suppress("OPT_IN_USAGE")
	compilerOptions {
		freeCompilerArgs = listOf("-Xexpect-actual-classes")
	}
}

android {
	namespace = "com.darkrockstudios.libraries.mpfilepicker"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_18
		targetCompatibility = JavaVersion.VERSION_18
	}
}