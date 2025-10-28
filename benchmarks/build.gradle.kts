plugins {
    java

    id("me.champeau.jmh") version "0.7.3"
}

repositories {
    mavenCentral()
}

dependencies {
    jmh(libs.jda)
    jmh(project(":ffm-impl"))
    jmh(project(":jna-impl"))
    jmh(project(":test-data"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release.set(22)
}

jmh {
    profilers = listOf("gc")
}
