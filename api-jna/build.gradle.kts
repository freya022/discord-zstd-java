plugins {
    `java-library`
}

project.group = rootProject.group
project.version = rootProject.version

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-api-jna"
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
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(8)
}
