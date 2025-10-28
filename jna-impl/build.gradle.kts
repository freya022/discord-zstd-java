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

    // JNA
    implementation(libs.jna)

    // JUnit 5 (JUnit 6 is not Java 8 compatible)
    testImplementation(libs.bundles.junit)
    testImplementation(project(":test-data"))
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

tasks.withType<Test> {
    useJUnitPlatform()
}

registerPublication(
    name = fullProjectName,
    description = "Zstandard streaming decompression API for JVM Discord API wrappers using Java Native Access (JNA)",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/jna-impl",
) {
    from(components["java"])
}
