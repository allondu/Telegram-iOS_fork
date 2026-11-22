plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Telegram application credentials. The defaults are the ones the OwpenGram
// Android client publishes (github.com/owpengram/owpengram-android-client), so a
// build of Courier works without asking the user for api_id/api_hash first.
// They are registered to the OwpenGram application: replace them with your own
// pair from my.telegram.org before publishing your own build.
val defaultApiId: String = "3893395"
val defaultApiHash: String = "e2adbe11ee0f092ced6043c4e051fdda"

val courierApiId: String = (project.findProperty("COURIER_API_ID") as String?)
    ?: System.getenv("COURIER_API_ID")
    ?: defaultApiId
val courierApiHash: String = (project.findProperty("COURIER_API_HASH") as String?)
    ?: System.getenv("COURIER_API_HASH")
    ?: defaultApiHash

android {
    namespace = "com.courier.messenger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.courier.messenger"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "COURIER_API_ID", "\"$courierApiId\"")
        buildConfigField("String", "COURIER_API_HASH", "\"$courierApiHash\"")

        ndk {
            // TDLib ships native code for these ABIs; keep the APK lean.
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    // Prebuilt official TDLib (org.drinkless.tdlib) with native libs for 4 ABIs.
    implementation("io.github.tdlib-android:core:0.1.1")
}
