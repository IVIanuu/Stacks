@file:Suppress("ClassName", "unused")

object Versions {
    // android
    const val compileSdk = 28
    const val minSdk = 14
    const val targetSdk = 28
    const val versionCode = 1
    const val versionName = "1.0"

    const val androidGradlePlugin = "3.3.0"
    const val kotlin = "1.3.21"
    const val mavenGradle = "2.1"
    const val support = "28.0.0"
}
//
object Deps {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"

    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    const val mavenGradlePlugin = "com.github.dcendents:android-maven-gradle-plugin:${Versions.mavenGradle}"

    const val supportAppCompat = "com.android.support:appcompat-v7:${Versions.support}"
    const val supportFragments = "com.android.support:support-fragment:${Versions.support}"
}