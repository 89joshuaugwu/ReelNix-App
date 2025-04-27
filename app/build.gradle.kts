plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Ensure the Kotlin Compose plugin is present
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.reelnixapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.reelnixapp"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    // Add the composeOptions block
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Or the latest compatible version
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // For Glance support (you had these correct!)
    implementation("androidx.glance:glance:1.1.1")
    implementation("androidx.glance:glance-appwidget:1.1.1")

    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Use the BOM for Compose (you had this correct, too!)
    implementation(platform(libs.androidx.compose.bom))
    // Now you can add individual Compose dependencies without version numbers
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Use the BOM here as well
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}