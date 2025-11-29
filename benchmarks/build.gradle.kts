plugins {
    `java-conventions`

    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    jmh(libs.jda)
    jmh(project(":jni-impl"))
    jmh(project(":test-data"))
}

jmh {
    profilers = listOf("gc")
}
