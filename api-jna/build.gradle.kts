plugins {
    `publish-conventions`
}

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-api-jna"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":api"))

    //Code safety
    compileOnly(libs.findbugs)

    //Logger
    implementation(libs.slf4j)

    // JNA
    implementation(libs.jna)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(8)
}

configurePublishedArtifact(
    artifactId = "discord-zstd-java-api-jna",
    description = "Zstandard streaming decompression API for JVM Discord API wrappers using Java Native Access (JNA)",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/api-jna",
    packaging = "jar"
)
