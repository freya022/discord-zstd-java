plugins {
    `java-conventions`
    kotlin("jvm") version "2.3.0"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.trove4j.core)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.logback.classic)
}
