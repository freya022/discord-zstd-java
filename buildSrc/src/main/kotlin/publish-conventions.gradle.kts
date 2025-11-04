import org.jreleaser.model.Active
import org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer

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
version = if (GitUtils.isGithubActions(providers)) {
    versionObj.toString()
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
                    stage = MavenCentralMavenDeployer.Stage.UPLOAD
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository(stagingDirectory.asFile.absolutePath)
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
