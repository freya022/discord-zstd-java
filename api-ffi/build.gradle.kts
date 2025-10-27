plugins {
    `publish-conventions`
}

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-api-ffi"
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
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(22)
}

configurePublishedArtifact(
    artifactId = "discord-zstd-java-api-ffi",
    description = "Zstandard streaming decompression API for JVM Discord API wrappers using the Foreign Function & Memory API",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/api-ffi",
    packaging = "jar"
)
