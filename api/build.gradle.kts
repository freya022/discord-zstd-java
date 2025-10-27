plugins {
    `publish-conventions`
}

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-api"
}

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly(project(":natives"))

    //Code safety
    compileOnly(libs.findbugs)

    //Logger
    implementation(libs.slf4j)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(8)
}

configurePublishedArtifact(
    artifactId = "${rootProject.name}-api",
    description = "Core of the Zstandard streaming decompression API for JVM Discord API wrappers",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/api",
    packaging = "jar"
)
