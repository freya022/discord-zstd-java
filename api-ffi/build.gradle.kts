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
    api(project(":api"))

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

    options.release.set(22)
}

registerPublication(
    name = fullProjectName,
    description = "Zstandard streaming decompression API for JVM Discord API wrappers using the Foreign Function & Memory API",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/api-ffi",
) {
    from(components["java"])
}
