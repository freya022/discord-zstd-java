import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

fun MavenPom.populate(moduleName: String, moduleDescription: String, moduleUrl: String) {
    packaging = "jar"
    
    name.set(moduleName)
    description.set(moduleDescription)
    url.set(moduleUrl)
    
    scm {
        url.set("https://github.com/freya022/discord-zstd-java")
        connection.set("scm:git://github.com/freya022/discord-zstd-java.git")
        developerConnection.set(connection)
    }

    licenses {
        license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
        }
    }
    
    developers {
        developer {
            name.set("freya022")
            email.set("41875020+freya022@users.noreply.github.com")
            url.set("https://github.com/freya022")
        }
    }
}

/**
 * Registers a [MavenPublication] named `Release` with the following configuration:
 *
 * - The group ID and version is copied from the current [Project] and **must** be strings
 * - The artifact ID is set to the provided [name]
 * - The POM is populated with the provided [name], [description] and [url] in addition to the common properties
 *
 * The [block] must attach inputs (`from(...)`, `artifact(...)`).
 */
fun Project.registerPublication(name: String, description: String, url: String, block: MavenPublication.() -> Unit) {
    extensions.configure<PublishingExtension> {
        publications {
            register<MavenPublication>("Release") {
                artifactId = name
                groupId = project.group as String
                version = project.version as String

                pom.populate(name, description, url)

                block()
            }
        }
    }
}
