plugins {
    `java-library`
    signing
    id("com.vanniktech.maven.publish")
}

val mavenCentralUsername: String? by project
val mavenCentralPassword: String? by project
/**
 * 1. Generate a key pair with `gpg --gen-key`, it will ask for a key name and an email address
 * 2. Start editing the key with `gpg --edit-key <key name>`, you should see a `gpg>` prompt
 * 3. (Optional) Modify the expiry of your primary key with `expire`
 * 4. If a subkey (`ssb`) was generated automatically, you can delete it by selecting it with `key 1` and running `delkey`
 * 5. Add a subkey with `addkey`, select `RSA (sign only)`
 * 6. Save everything with `save`, it will exit the gpg prompt
 * 7. Show the subkey IDs with `gpg -K --keyid-format short`, you should see two keys:
 *    - `sec ed25519/<primary key id> <created on> [SC]`, with the line below being the public key
 *    - `ssb rsa<length>/<subkey id> <created on> [S]`
 * 8. Set `mavenGpgKeyId` with the subkey id
 * 9. Set `mavenGpgSecretKey` with the secret key using `gpg --export-secret-key --armor <public key>`
 */
val mavenGpgKeyId: String? by project
val mavenGpgSecretKey: String? by project

val canSign = mavenGpgKeyId != null && mavenGpgSecretKey != null
val canPublish = mavenCentralUsername != null && mavenCentralPassword != null && canSign

group = "dev.freya02"
version = Version(
    major = property("version.major").toString(),
    minor = property("version.minor").toString(),
    revision = property("version.revision").toString(),
    classifier = property("version.classifier").toString(),
    // isRelease = isCi || canPublish
    isDev = !GitUtils.isCI(providers) && !canPublish
)

val effectiveTag: String? = if (canPublish) {
    GitUtils.getHeadTag(logger, providers, projectDir.absolutePath) ?: error("Attempted to publish on a non-release commit")
} else {
    null
}

tasks.withType<Javadoc> {
    val options = options as StandardJavadocDocletOptions

    options.memberLevel = JavadocMemberLevel.PUBLIC
    options.encoding = "UTF-8"

    options.addBooleanOption("-no-fonts", true)
}

signing {
    isRequired = canPublish

    useInMemoryPgpKeys(mavenGpgKeyId, mavenGpgSecretKey, "")
}

mavenPublishing {
    if (canPublish) {
        publishToMavenCentral(automaticRelease = true)

        signAllPublications()
    }

    pom {
        // Description, URL, artifact ID, name and packaging are set in 'configurePublishedArtifact'

        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }

        developers {
            developer {
                name = "freya022"
                email = "41875020+freya022@users.noreply.github.com"
                url = "https://github.com/freya022"
            }
        }

        scm {
            connection = "scm:git:https://github.com/freya022/discord-zstd-java.git"
            developerConnection = connection
            url = "https://github.com/freya022/discord-zstd-java"
            tag = effectiveTag
        }
    }
}

afterEvaluate {
    check(isPublishedArtifactConfigured(project)) {
        "Project '${project.path}' did not call 'configurePublishedArtifact'"
    }
}
