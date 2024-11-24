// build.gradle.kts (Project Level)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {

    }
    dependencies {
        // Make sure you have the correct version of the Android Gradle plugin and Kotlin plugin
        classpath("com.android.tools.build:gradle:7.4.0")  // Adjust to the latest version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")  // Adjust Kotlin version as needed
    }
}

allprojects {
    repositories {

    }
}
