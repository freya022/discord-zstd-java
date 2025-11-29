plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // Modules that don't specify a "release" version will be set to the toolchain version
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}
