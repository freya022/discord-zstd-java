plugins {
    `java-conventions`
}

dependencies {
    implementation(libs.jackson.databind)
}

val convert by tasks.registering(JavaExec::class) {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "ResultsConverter"
    args(project(":benchmarks").layout.buildDirectory.file("results/jmh/results.json").get().asFile.absolutePath)
}
