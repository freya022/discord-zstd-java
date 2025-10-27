import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign

private val configuredPublishedArtifacts = hashSetOf<String>()

fun isPublishedArtifactConfigured(project: Project) = project.path in configuredPublishedArtifacts

/**
 * Sets the provided [artifactId] as the Kotlin module name, Dokka module name & path, and Maven artifact ID.
 */
fun Project.configurePublishedArtifact(artifactId: String, description: String, url: String, packaging: String) {
    val artifactPrefix = rootProject.name
    check(artifactId.startsWith(artifactPrefix)) {
        "Artifact ID must start with '$artifactPrefix'"
    }

    configuredPublishedArtifacts += this.path

    extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
        coordinates(artifactId = artifactId)

        pom {
            // Sonatype requires
            this.name = artifactId
            this.packaging = packaging

            this.description = description
            this.url = url
        }
    }
}
