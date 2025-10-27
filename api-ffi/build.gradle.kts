plugins {
    `java-library`
}

project.group = rootProject.group
project.version = rootProject.version

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-api-ffi"
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
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(22)
}
