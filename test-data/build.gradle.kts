plugins {
    `java-conventions`
    `java-library`
}

dependencies {
    compileOnly(libs.jspecify)
}

tasks.withType<Test> {
    enabled = false
}

val generateTestData by tasks.registering(TestDataGeneratorTask::class)
