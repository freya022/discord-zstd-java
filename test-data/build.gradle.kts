plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.findbugs)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release = 8
}
