plugins {
    id("com.android.application")
}

android {
    namespace = "com.vhznyo.nothing_osc_glyphs"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vhznyo.nothing_osc_glyphs"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("libs\\KetchumSDK_Community_20231123.jar"))
    implementation(files("libs\\log4j-1.2.15.jar"))
    implementation(files("libs\\mina-core-2.0.0-M3.jar"))
    implementation(files("libs\\slf4j-api-1.5.0.jar"))
    implementation(files("libs\\slf4j-log4j12-1.5.0.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}