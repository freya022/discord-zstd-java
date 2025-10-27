plugins {
    `java-library`
}

project.group = rootProject.group
project.version = rootProject.version

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-natives"
}
