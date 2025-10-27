import org.jreleaser.model.Active

plugins {
    `maven-publish`

    id("org.jreleaser")
}

val versionObj = Version(
    major = property("version.major").toString(),
    minor = property("version.minor").toString(),
    revision = property("version.revision").toString(),
    classifier = property("version.classifier").toString(),
)

group = "dev.freya02"
version = if (GitUtils.isCI(providers)) {
    val commitHash = System.getenv("GIT_COMMIT")
        ?: System.getenv("GITHUB_SHA")
        ?: error("Attempted to publish but no Git commit hash could be found")
    "${versionObj}_$commitHash"
} else {
    "${versionObj}_DEV"
}

val stagingDirectory = layout.buildDirectory.dir("staging-deploy").get()

publishing {
    repositories.maven(url = stagingDirectory.asFile.toURI())
}

jreleaser {
    // For subprojects
    gitRootSearch = true

    project {
        versionPattern = "CUSTOM"
    }

    release {
        github {
            enabled = false
        }
    }

    signing {
        active = Active.RELEASE
        armored = true
    }

    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository(stagingDirectory.asFile.relativeTo(projectDir).path)
                }
            }
        }
    }
}

tasks.withType<Javadoc> {
    val options = options as StandardJavadocDocletOptions

    options.memberLevel = JavadocMemberLevel.PUBLIC
    options.encoding = "UTF-8"

    options.addBooleanOption("-no-fonts", true)
    options.addBooleanOption("Xdoclint:all,-missing", true)
}
