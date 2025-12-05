package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

/**
 * Factory of {@link DiscordZstdDecompressor}.
 *
 * <p>Instances are thread safe.
 */
@NullMarked
public interface DiscordZstdDecompressorFactory {
    /**
     * Creates a new {@link DiscordZstdDecompressor} configured with the parameters passed to this factory.
     *
     * @return A new {@link DiscordZstdDecompressor} instance
     */
    DiscordZstdDecompressor create();
}
