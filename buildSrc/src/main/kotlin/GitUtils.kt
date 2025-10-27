import org.gradle.api.provider.ProviderFactory

object GitUtils {

    fun isCI(providers: ProviderFactory): Boolean {
        return providers.environmentVariable("GIT_COMMIT").isPresent // Jitpack
                || providers.environmentVariable("GITHUB_ACTIONS").isPresent // GitHub Actions
    }
}
