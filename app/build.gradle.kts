plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.makefoods"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.makefoods"
        minSdk = 26
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
}

// ✅ 이 프로젝트에서만 버전 강제 고정 (다른 프로젝트에 영향 없음)
configurations.all {
    resolutionStrategy {
        // activity 라이브러리를 1.9.3으로 강제 고정
        force("androidx.activity:activity:1.9.3")
        force("androidx.activity:activity-ktx:1.9.3")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ 명시적으로 1.9.3 버전 사용
    implementation("androidx.activity:activity:1.9.3")
}