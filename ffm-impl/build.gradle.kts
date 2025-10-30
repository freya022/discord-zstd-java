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
    compileOnly(libs.jspecify)

    //Logger
    implementation(libs.slf4j)

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

    options.release.set(22)
}

tasks.withType<Test> {
    useJUnitPlatform()

    filter {
        isFailOnNoMatchingTests = false

        // Only the CI will put the natives in the expected spot
        if (!GitUtils.isGithubActions(providers)) {
            excludeTestsMatching("ZstdFFMTest")
        }
    }
}

registerPublication(
    name = fullProjectName,
    description = "Zstandard streaming decompression API for JVM Discord API wrappers using the Foreign Function & Memory API",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/ffm-impl",
) {
    from(components["java"])
}
