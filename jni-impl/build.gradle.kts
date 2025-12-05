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
    api(project(":api"))

    //Code safety
    compileOnly(libs.jspecify)

    //Logger
    implementation(libs.slf4j)

    // JUnit 5 (JUnit 6 is not Java 8 compatible)
    testImplementation(libs.bundles.junit)
    testImplementation(project(":test-data"))

    testRuntimeOnly(libs.logback.classic)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

tasks.withType<Test> {
    useJUnitPlatform()

    filter {
        isFailOnNoMatchingTests = false

        // Only the CI will put the natives in the expected spot
        if (!GitUtils.isGithubActions(providers)) {
            excludeTestsMatching("ZstdJNITest")
        }
    }
}

registerPublication(
    name = fullProjectName,
    description = "Zstandard streaming decompression API for JVM Discord API wrappers using Java Native Interface (JNI)",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/jni-impl",
) {
    from(components["java"])
}
