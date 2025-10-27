plugins {
    `publish-conventions`
}

tasks.withType<Jar> {
    archiveBaseName = "${rootProject.name}-natives"
}

configurePublishedArtifact(
    artifactId = "discord-zstd-java-natives",
    description = "Minimal Zstandard decompression natives for JVM Discord API wrappers",
    url = "https://github.com/freya022/discord-zstd-java/tree/master/natives",
    packaging = "jar"
)
