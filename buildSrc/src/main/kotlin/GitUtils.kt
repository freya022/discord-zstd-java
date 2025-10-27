import org.gradle.api.provider.ProviderFactory

object GitUtils {

    fun isGithubActions(providers: ProviderFactory): Boolean {
        return providers.environmentVariable("GITHUB_ACTIONS").isPresent
    }
}
