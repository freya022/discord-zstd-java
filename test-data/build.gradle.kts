plugins {
    `java-conventions`
    `java-library`
}

dependencies {
    compileOnly(libs.jspecify)
}

val generateTestData by tasks.registering(TestDataGeneratorTask::class)
