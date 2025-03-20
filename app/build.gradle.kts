import java.net.URI
import java.util.Properties
import java.io.StringReader
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
}

val envUrl = "https://raw.githubusercontent.com/asfu222/BACNLocalizationResources/refs/heads/main/ba.env"
val envText = URI.create(envUrl).toURL().readText(Charsets.UTF_8)
val envProps = Properties().apply {
    load(StringReader(envText))
}

android {
    namespace = "com.asfu222.bajpdl"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.asfu222.bajpdl"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (System.getenv("KEYSTORE_FILE")?.isNotBlank() == true) {
            create("release") {
                val keystoreFileContent = System.getenv("KEYSTORE_FILE")
                val keystoreFile = File(layout.buildDirectory.asFile.get(), "keystore.jks")
                keystoreFile.writeBytes(Base64.getDecoder().decode(keystoreFileContent))

                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        aidl = true
    }

    flavorDimensions("version")

    productFlavors {
        create("mainBuild") {
            dimension = "version"
            applicationId = "com.asfu222.bajpdl"
            // Apply signing config for this flavor
            signingConfig = signingConfigs.findByName("release")
        }

        create("mitmBuild") {
            dimension = "version"
            applicationId = "com.YostarJP.BlueArchive"
            versionCode = envProps.getProperty("BA_VERSION_CODE").toInt()
            versionName = envProps.getProperty("BA_VERSION_NAME")
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

dependencies {
    implementation(libs.lz4.java)
    implementation(libs.json)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.okhttp)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
