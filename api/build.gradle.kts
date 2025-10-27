plugins {
    `java-library`
    `publish-conventions`
}

val fullProjectName = "${rootProject.name}-${project.name}"

tasks.withType<Jar> {
    archiveBaseName = fullProjectName
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

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(8)
}

registerPublication(
    name = fullProjectName,
    description = "Core of the Zstandard streaming decompression API for JVM Discord API wrappers",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/api",
) {
    from(components["java"])
}
