plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.liytu.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)

    implementation(libs.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.jsoup)

    implementation(libs.zip4j)
    implementation(libs.pdfbox.android)
    implementation(libs.epublib)
}
