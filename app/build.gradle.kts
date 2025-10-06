plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.pandutimurbhaskara.compose_media"
	compileSdk {
		version = release(36)
	}

	defaultConfig {
		applicationId = "com.pandutimurbhaskara.compose_media"
		minSdk = 28
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

	// ML Kit Face Detection
	implementation("com.google.mlkit:face-detection:16.1.6")

	// ML Kit Text Recognition
	implementation("com.google.mlkit:text-recognition:16.0.0")

	// Image processing
	implementation("androidx.exifinterface:exifinterface:1.3.7")

	// Coroutines for async processing
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

	// ViewModel Compose
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}