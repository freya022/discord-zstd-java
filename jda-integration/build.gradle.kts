plugins {
    `java-conventions`
    `java-library`
    `publish-conventions`
}

val fullProjectName = "${rootProject.name}-${project.name}"

tasks.withType<Jar> {
    archiveBaseName = fullProjectName
}

repositories {
    mavenLocal()
}

val mockitoAgent by configurations.creating
dependencies {
    implementation(project(":api"))
    runtimeOnly(project(":jni-impl"))

    implementation(libs.jda.snapshot)

    //Code safety
    compileOnly(libs.jspecify)
    testCompileOnly(libs.jspecify)

    //Logger
    implementation(libs.slf4j)

    // JUnit 5 (JUnit 6 is not Java 8 compatible)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.mockito)
    mockitoAgent(libs.mockito) { isTransitive = false }
    testImplementation(libs.assertj)

    testRuntimeOnly(libs.logback.classic)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

tasks.test {
    useJUnitPlatform()
    failFast = false

    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}

registerPublication(
    name = fullProjectName,
    description = "Lightweight Zstandard decompressor for JDA",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/jda-integration",
) {
    from(components["java"])
}
