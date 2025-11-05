package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.util.ServiceLoader;

/**
 * Provider of {@link ZstdDecompressorFactory}.
 *
 * <p>Instances are thread safe and can be obtained using {@link ServiceLoader}.
 */
@NullMarked
public interface ZstdDecompressorFactoryProvider {
    /**
     * Creates a new {@link ZstdDecompressorFactory} with the provided decompression buffer size.
     *
     * @param  bufferSizeHint
     *         A hint for the size of the buffer used for decompression,
     *         must be larger than {@value ZstdDecompressor#MIN_BUFFER_SIZE} or be equal to {@value ZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE}.
     *         Typically, bigger buffers mean less decompression loops, it does not change inputs or outputs
     *
     * @throws IllegalArgumentException
     *         If {@code bufferSize} is less than {@value ZstdDecompressor#MIN_BUFFER_SIZE} and not {@value ZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE}
     *
     * @return A new {@link ZstdDecompressorFactory} instance
     */
    ZstdDecompressorFactory get(int bufferSizeHint);
}
