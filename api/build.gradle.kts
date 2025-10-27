plugins {
    `java-library`
}

project.group = rootProject.group
project.version = rootProject.version

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-api"
}

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly(project(":natives"))

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

    options.release.set(8)
}
