package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Entrypoint of {@code discord-zstd-java}. This is where you can get instances of {@link DiscordZstd}.
 */
@NullMarked
public class DiscordZstdProvider {

    private static final class InstanceHolder {
        private static final DiscordZstd INSTANCE = load();
    }

    /**
     * Loads the first implementation of {@link DiscordZstd} found on the classpath.
     *
     * @throws ServiceConfigurationError If the provider failed to load
     *
     * @return An implementation of {@link DiscordZstd}
     */
    public static DiscordZstd get() {
        return InstanceHolder.INSTANCE;
    }

    private static DiscordZstd load() {
        Iterator<DiscordZstd> instances = ServiceLoader.load(DiscordZstd.class).iterator();
        if (!instances.hasNext()) {
            throw new IllegalStateException("No implementations of discord-zstd-java could be found, make sure you added the dependency as described in https://github.com/freya022/discord-zstd-java/blob/master/README.md#-for-bot-developers");
        }

        return instances.next();
    }
}
