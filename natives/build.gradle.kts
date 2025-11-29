plugins {
    `java-conventions`
    `java-library`
    `publish-conventions`
}

val fullProjectName = "${rootProject.name}-${project.name}"

tasks.withType<Jar> {
    archiveBaseName = fullProjectName
}

java {
    withJavadocJar()
    withSourcesJar()
}

// There are no sources but Gradle will say the lib requires [current JDK] if we don't do that
tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

registerPublication(
    name = fullProjectName,
    description = "Minimal Zstandard decompression natives for JVM Discord API wrappers",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/natives",
) {
    from(components["java"])
}
