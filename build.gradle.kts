plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.milauncher"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.milauncher"
        minSdk = 26
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("androidx.recyclerview:recyclerview:1.3.2") }
