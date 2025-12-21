plugins {
    `java-conventions`
    kotlin("jvm") version "2.3.0"
}

dependencies {
    implementation(libs.jda)
    implementation(libs.jackson.databind)
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-parameters")
    }
}

val generate by tasks.registering(JavaExec::class) {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "dev.freya02.discord.zstd.generator.TestDataGeneratorKt"
    args(rootProject.layout.projectDirectory.dir("gateway-chunks-zlib").asFile.absolutePath)
}
