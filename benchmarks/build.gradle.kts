plugins {
    `java-conventions`

    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    jmh(libs.jda)
    jmh(libs.jackson.databind)
    jmh(project(":jni-impl"))
    jmh(project(":test-data"))
}

jmh {
    profilers = listOf("gc", "perfnorm")
    resultFormat = "JSON"
    humanOutputFile = project.file("${project.layout.buildDirectory.asFile.get()}/reports/jmh/human.txt")
}
