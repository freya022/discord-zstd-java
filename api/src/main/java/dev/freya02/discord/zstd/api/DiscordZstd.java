package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.io.InputStream;

/**
 * Main interface which allows creating {@linkplain DiscordZstdContext decompression contexts},
 * and {@linkplain DiscordZstdDecompressorFactory decompressor factories}.
 */
@NullMarked
public interface DiscordZstd {
    /**
     * Creates a new {@link DiscordZstdContext}.
     * <br>This is used to keep track of streaming decompression after each input is consumed via an {@link InputStream}.
     *
     * @return A new {@link DiscordZstdContext} instance
     */
    DiscordZstdContext createContext();

    /**
     * Creates a new {@link DiscordZstdDecompressorFactory} with the provided decompression buffer size.
     *
     * @param  bufferSizeHint
     *         A hint for the size of the buffer used for decompression,
     *         must be larger than {@value DiscordZstdDecompressor#MIN_BUFFER_SIZE} or be equal to {@value DiscordZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE}.
     *         <br>Typically, bigger buffers mean less decompression loops, it does not change inputs or outputs
     *
     * @throws IllegalArgumentException
     *         If {@code bufferSize} is less than {@value DiscordZstdDecompressor#MIN_BUFFER_SIZE} and not {@value DiscordZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE}
     *
     * @return A new {@link DiscordZstdDecompressorFactory} instance
     */
    DiscordZstdDecompressorFactory createDecompressorFactory(int bufferSizeHint);
}
