plugins {
    `java-conventions`
    `java-library`
}

val fullProjectName = "${rootProject.name}-${project.name}"

tasks.withType<Jar> {
    archiveBaseName = fullProjectName
}

dependencies {
    api(project(":api"))
    runtimeOnly(project(":natives"))

    //Code safety
    compileOnly(libs.jspecify)

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

tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

tasks.withType<Test> {
    useJUnitPlatform()

    filter {
        isFailOnNoMatchingTests = false

        // Only the CI will put the natives in the expected spot
        if (!GitUtils.isGithubActions(providers)) {
            excludeTestsMatching("ZstdJNATest")
        }
    }
}
