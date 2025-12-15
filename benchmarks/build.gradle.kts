plugins {
    `java-conventions`

    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    testImplementation(libs.bundles.junit)
    testImplementation(project(":test-data"))
    testImplementation(libs.jda)

    jmh(libs.jda)
    jmh(libs.jackson.databind)
    jmh(project(":jni-impl"))
    jmh(project(":test-data"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jmh {
    profilers = listOf("gc", "perfnorm")
    resultFormat = "JSON"
    humanOutputFile = project.file("${project.layout.buildDirectory.asFile.get()}/reports/jmh/human.txt")
}
