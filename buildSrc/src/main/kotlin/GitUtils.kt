import org.gradle.api.logging.Logger
import org.gradle.api.provider.ProviderFactory

object GitUtils {

    fun isCI(providers: ProviderFactory): Boolean {
        return (providers.systemProperty("BUILD_NUMBER").isPresent // Jenkins
                || providers.environmentVariable("BUILD_NUMBER").isPresent
                || providers.systemProperty("GIT_COMMIT").isPresent // Jitpack
                || providers.environmentVariable("GIT_COMMIT").isPresent
                || providers.systemProperty("GITHUB_ACTIONS").isPresent // GitHub Actions
                || providers.environmentVariable("GITHUB_ACTIONS").isPresent)
    }

    fun getHeadTag(logger: Logger, providers: ProviderFactory, directory: String): String? {
        try {
            val output = providers.exec {
                commandLine("git", "describe", "--tags", "--abbrev=0", "--exact-match")
                workingDir(directory)
                isIgnoreExitValue = true
            }

            if (output.result.get().exitValue == 128) { return null }

            return output.standardOutput.asText.get().lineSequence().first()
        } catch (e: Exception) {
            logger.error("Unable to get head tag", e)
            return null
        }
    }
}
