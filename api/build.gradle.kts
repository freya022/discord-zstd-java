plugins {
    `java-conventions`
    `java-library`
    `publish-conventions`
}

val fullProjectName = "${rootProject.name}-${project.name}"

tasks.withType<Jar> {
    archiveBaseName = fullProjectName
}

dependencies {
    //Code safety
    compileOnly(libs.jspecify)

    //Logger
    implementation(libs.slf4j)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "discord.zstd.java.api")
    }
}

registerPublication(
    name = fullProjectName,
    description = "Core of the Zstandard streaming decompression API for JVM Discord API wrappers",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/api",
) {
    from(components["java"])
}
